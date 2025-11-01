package edu.washington.cse332.autograder.concurrent;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Fork Join Graph. Produced as output from ForkJoinAnalyzer.
 *
 * @author Albert Du
 */
public final class ForkJoinGraph {
    /**
     * Represents a single execution segment.
     * Enter Exit, and Joins form boundaries for each task id
     */
    private static class Segment {
        private final long _taskId;
        private final int segmentId;
        private final ForkJoinEvent _startEvent;
        private final ForkJoinEvent _finishEvent;

        Segment(ForkJoinEvent startEvent, ForkJoinEvent finishEvent, int segId) {
            if (startEvent.getTaskId() != finishEvent.getTaskId()) {
                throw new IllegalArgumentException("startEvent must have the same taskId");
            }

            _taskId = startEvent.getTaskId();
            _startEvent = startEvent;
            _finishEvent = finishEvent;
            segmentId = segId;
        }

        public long getTaskId() {
            return _taskId;
        }

        public long getSegmentId() {
            return segmentId;
        }

        public long getNanoSeconds() {
            return _finishEvent.getTimestamp() - _startEvent.getTimestamp();
        }

        public long getStartTime() {
            return _startEvent.getTimestamp();
        }

        public long getEndTime() {
            return _finishEvent.getTimestamp();
        }

        @Override
        public int hashCode() {
            return (int) _taskId ^ (int) (_taskId << 32) ^ segmentId;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Segment seg
                    && seg.segmentId == segmentId
                    && seg._taskId == _taskId;
        }

        @Override
        public String toString() {
            return _taskId + "_" + segmentId;
        }
    }

    /**
     * A directed graph edge. start -> end iff end must wait for start to finish
     * before itself beginning.
     *
     * @param start
     * @param end
     * @param type  The reason for this edge's addition.
     */
    private record Edge(Segment start, Segment end, Type type) {
        public enum Type {
            /**
             * In a single task, following computes dependent on their antecedents.
             */
            Sequential,
            /**
             * A task calling a different task's compute method.
             */
            Compute,
            /**
             * A compute finishing, the parent's next task depends on it.
             */
            ComputeFinished,
            /**
             * A new task starts
             */
            Fork,
            /**
             * Parent must wait for child to finish
             */
            Join
        }
    }

    // Fields:
    private final List<Segment> _segments;
    private final List<Edge> _edges;
    private final Segment _root;

    private ForkJoinGraph(List<Segment> segments, List<Edge> edges, Segment root) {
        _segments = segments;
        _edges = edges;
        _root = root;
    }

    static ForkJoinGraph create(List<ForkJoinEvent> events) {
        // If there are no events, return an empty graph
        if (events.size() == 0)
            return new ForkJoinGraph(new ArrayList<Segment>(), new ArrayList<Edge>(), null);

        // Normalize per task and sort by time.
        final var perTask = groupByTaskSorted(events);

        // For each task, compute wait intervals (Compute -> ComputeFinished for same
        // child)
        final var waitIntervals = computeWaitIntervals(perTask);

        // Build active segments only (skip waits & zero-length)
        final var activeByTask = buildActiveSegments(perTask, waitIntervals);

        // Build edges:
        // - Sequential within each task (active-only)
        // - Fork / Join / Compute / ComputeFinished across tasks, snapped to nearest
        // active segments
        final var edges = new ArrayList<Edge>();
        addSequentialEdges(activeByTask, edges);
        addCausalEdges(events, activeByTask, edges);

        // Pick the earliest active segment as root (must exist)
        final var root = activeByTask.values().stream()
                .flatMap(List::stream)
                .min(Comparator.comparingLong(Segment::getStartTime))
                .orElseThrow(() -> new IllegalStateException("No active segments constructed"));

        // Flatten active segments for fields
        final var allActive = activeByTask.values().stream().flatMap(Collection::stream).collect(Collectors.toList());

        return new ForkJoinGraph(allActive, edges, root);
    }

    public double structuralWork() { // # of active segments
        return _segments.size();
    }

    public double empiricalWork() {
        return _segments.stream().map(Segment::getNanoSeconds).reduce(Long::sum).orElse(0L);
    }

    public double structuralCriticalPath() {
        if (_segments.isEmpty())
            return 0.0;

        var topo = topoOrder();
        var succ = successorsMap();

        Map<Segment, Integer> dist = new HashMap<>();
        int best = 0;
        for (Segment u : topo) {
            int du = Math.max(dist.getOrDefault(u, 0), 1); // unit weight
            for (Segment v : succ.getOrDefault(u, List.of())) {
                dist.put(v, Math.max(dist.getOrDefault(v, 0), du + 1));
            }
            best = Math.max(best, du);
        }
        return best > 0 ? best : 1;
    }

    public double empiricalCriticalPath() {
        if (_segments.isEmpty())
            return 0.0;

        var topo = topoOrder();
        var succ = successorsMap();

        Map<Segment, Integer> dist = new HashMap<>();
        int best = 0;

        for (Segment u : topo) {
            int du = dist.getOrDefault(u, 0);
            for (Segment v : succ.getOrDefault(u, List.of())) {
                dist.put(v, Math.max(dist.getOrDefault(v, 0), du + (int) u.getNanoSeconds()));
            }
            best = Math.max(best, du + (int) u.getNanoSeconds());
        }

        return best > 0 ? best : 1;
    }

    public double structuralSpeedup() {
        return structuralWork() / structuralCriticalPath();
    }

    public double empiricalSpeedup() {
        return empiricalWork() / empiricalCriticalPath();
    }

    public String toDOT() {
        return toDOT(true);
    }

    public String toDOT(boolean fancy) {
        var sb = new StringBuilder();
        sb.append("digraph G {\n");

        if (!fancy)
            sb.append("  node[fontcolor=\"white\"];\n");

        for (var seg : _segments) {
            sb.append("  \"").append(seg).append("\";\n");
        }

        sb.append("\n");

        for (var e : _edges) {
            final var color = !fancy
                    ? "black"
                    : switch (e.type) {
                        case Sequential -> "black";
                        case Fork -> "green";
                        case Join -> "blue";
                        case Compute -> "red";
                        case ComputeFinished -> "orange";
                    };
            sb.append("  \"").append(e.start).append("\" -> \"").append(e.end).append("\" [color=").append(color)
                    .append("];\n");
        }

        sb.append("}");
        return sb.toString();
    }

    private Map<Segment, List<Segment>> successorsMap() {
        final var map = new HashMap<Segment, List<Segment>>();
        for (Edge e : _edges)
            map.computeIfAbsent(e.start(), k -> new ArrayList<>()).add(e.end());

        return map;
    }

    private List<Segment> topoOrder() {
        // DFS from root over edges (ACTIVE-only graph)
        final var order = new ArrayList<Segment>();
        final var seen = new HashSet<Segment>();
        dfs(_root, seen, order);
        Collections.reverse(order);
        return order;
    }

    private void dfs(Segment u, Set<Segment> seen, List<Segment> out) {
        if (!seen.add(u))
            return;

        for (var e : _edges) {
            if (e.start().equals(u))
                dfs(e.end(), seen, out);
        }
        out.add(u);
    }

    private static Map<Long, List<ForkJoinEvent>> groupByTaskSorted(List<ForkJoinEvent> events) {
        final var perTask = new HashMap<Long, List<ForkJoinEvent>>();
        for (var e : events)
            perTask.computeIfAbsent(e.getTaskId(), k -> new ArrayList<>()).add(e);
        for (var list : perTask.values())
            list.sort(Comparator.comparingLong(ForkJoinEvent::getTimestamp));
        return perTask;
    }

    private static Map<Long, List<long[]>> computeWaitIntervals(Map<Long, List<ForkJoinEvent>> perTask) {
        // For each task, track [start,end) intervals where the task is waiting on a
        // specific child
        var waits = new HashMap<Long, List<long[]>>();
        for (var entry : perTask.entrySet()) {
            long taskId = entry.getKey();
            var evts = entry.getValue();
            // childId -> startTime stack (allow nested/spurious patterns defensively)
            Map<Long, Deque<Long>> open = new HashMap<>();
            List<long[]> intervals = new ArrayList<>();

            for (ForkJoinEvent e : evts) {
                if (e instanceof ForkJoinEvent.ComputeEvent ce) {
                    open.computeIfAbsent(ce.getChildId(), k -> new ArrayDeque<>()).push(e.getTimestamp());
                } else if (e instanceof ForkJoinEvent.ComputeFinishedEvent cf) {
                    var stk = open.get(cf.getChildId());
                    if (stk != null && !stk.isEmpty()) {
                        long start = stk.pop();
                        long end = e.getTimestamp();
                        if (end > start)
                            intervals.add(new long[] { start, end });
                    }
                }
            }
            intervals.sort(Comparator.comparingLong(a -> a[0]));
            waits.put(taskId, intervals);
        }
        return waits;
    }

    private static boolean withinAny(long t0, long t1, List<long[]> intervals) {
        if (intervals == null || intervals.isEmpty())
            return false;
        // intervals are non-overlapping, sorted by start; the segment [t0,t1) is
        // between adjacent task events
        int lo = 0, hi = intervals.size() - 1, idx = -1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            long[] it = intervals.get(mid);
            if (it[0] <= t0) {
                idx = mid;
                lo = mid + 1;
            } else
                hi = mid - 1;
        }
        int i = Math.max(0, idx);
        for (; i < intervals.size(); i++) {
            long[] it = intervals.get(i);
            if (it[0] >= t1)
                break;
            // overlap if it.start < t1 && it.end > t0
            if (it[1] > t0)
                return true;
        }
        return false;
    }

    private static Map<Long, List<Segment>> buildActiveSegments(Map<Long, List<ForkJoinEvent>> perTask,
            Map<Long, List<long[]>> waitIntervals) {
        Map<Long, List<Segment>> active = new HashMap<>();
        for (var entry : perTask.entrySet()) {
            long taskId = entry.getKey();
            var evts = entry.getValue();
            var waits = waitIntervals.get(taskId);

            var segs = new ArrayList<Segment>();
            for (int i = 0; i + 1 < evts.size(); i++) {
                ForkJoinEvent a = evts.get(i), b = evts.get(i + 1);
                long t0 = a.getTimestamp(), t1 = b.getTimestamp();
                if (t1 <= t0)
                    continue; // zero/negative -> ignore
                if (withinAny(t0, t1, waits))
                    continue; // skip wait time
                segs.add(new Segment(a, b, segs.size()));
            }
            if (!segs.isEmpty())
                active.put(taskId, segs);
        }
        return active;
    }

    private static void addSequentialEdges(Map<Long, List<Segment>> activeByTask, List<Edge> edges) {
        for (var entry : activeByTask.entrySet()) {
            var segs = entry.getValue();
            for (int i = 0; i + 1 < segs.size(); i++) {
                edges.add(new Edge(segs.get(i), segs.get(i + 1), Edge.Type.Sequential));
            }
        }
    }

    private static void addCausalEdges(List<ForkJoinEvent> events,
            Map<Long, List<Segment>> activeByTask,
            List<Edge> edges) {
        for (var e : events) {
            if (e instanceof ForkJoinEvent.ForkEvent fe) {
                var p = lastActiveEndingAtOrBefore(activeByTask.get(fe.getTaskId()), fe.getTimestamp());
                var c = firstActive(activeByTask.get(fe.getChildId()));
                if (p != null && c != null)
                    edges.add(new Edge(p, c, Edge.Type.Fork));

            } else if (e instanceof ForkJoinEvent.JoinEvent je) {
                var cLast = lastActive(activeByTask.get(je.getChildId()));
                var pNext = firstActiveStartingAtOrAfter(activeByTask.get(je.getTaskId()), je.getTimestamp());
                if (cLast != null && pNext != null)
                    edges.add(new Edge(cLast, pNext, Edge.Type.Join));

            } else if (e instanceof ForkJoinEvent.ComputeEvent ce) {
                var p = lastActiveEndingAtOrBefore(activeByTask.get(ce.getTaskId()), ce.getTimestamp());
                var cFirst = firstActive(activeByTask.get(ce.getChildId()));
                if (p != null && cFirst != null)
                    edges.add(new Edge(p, cFirst, Edge.Type.Compute));

            } else if (e instanceof ForkJoinEvent.ComputeFinishedEvent cfe) {
                var cLast = lastActive(activeByTask.get(cfe.getChildId()));
                var pNext = firstActiveStartingAtOrAfter(activeByTask.get(cfe.getTaskId()), cfe.getTimestamp());
                if (cLast != null && pNext != null)
                    edges.add(new Edge(cLast, pNext, Edge.Type.ComputeFinished));
            }
        }
    }

    private static Segment firstActive(List<Segment> segs) {
        if (segs == null || segs.isEmpty())
            return null;
        return segs.getFirst();
    }

    private static Segment lastActive(List<Segment> segs) {
        if (segs == null || segs.isEmpty())
            return null;
        return segs.getLast();
    }

    private static Segment lastActiveEndingAtOrBefore(List<Segment> segs, long ts) {
        if (segs == null)
            return null;
        Segment cand = null;
        for (var s : segs) {
            if (s.getEndTime() <= ts)
                cand = s;
            else
                break;
        }
        return cand;
    }

    private static Segment firstActiveStartingAtOrAfter(List<Segment> segs, long ts) {
        if (segs == null)
            return null;
        for (var s : segs) {
            if (s.getStartTime() >= ts)
                return s;
        }
        return null;
    }
}
