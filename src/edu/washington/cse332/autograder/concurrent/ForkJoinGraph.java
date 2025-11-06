package edu.washington.cse332.autograder.concurrent;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Fork Join Graph. Produced as output from {@link ForkJoinAnalyzer}.
 *
 * @author Albert Du
 */
public final class ForkJoinGraph {

    // #region Fields
    private final List<ForkJoinSegment> _segments;
    private final List<ForkJoinEdge> _edges;
    private final ForkJoinSegment _root;
    // #endregion

    // #region Constructor and Factory
    private ForkJoinGraph(List<ForkJoinSegment> segments, List<ForkJoinEdge> edges, ForkJoinSegment root) {
        _segments = segments;
        _edges = edges;
        _root = root;
    }

    static ForkJoinGraph create(List<ForkJoinEvent> events) {
        // If there are no events, return an empty graph
        if (events.size() == 0)
            return new ForkJoinGraph(Collections.emptyList(), Collections.emptyList(), null);

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
        final var edges = new ArrayList<ForkJoinEdge>();
        addSequentialEdges(activeByTask, edges);
        addCausalEdges(events, activeByTask, edges);

        // Pick the earliest active segment as root (must exist)
        final var root = activeByTask.values().stream()
                .flatMap(List::stream)
                .min(Comparator.comparingLong(ForkJoinSegment::getStartTime))
                .orElseThrow(() -> new IllegalStateException("No active segments constructed"));

        // Flatten active segments for fields
        final var allActive = activeByTask.values().stream().flatMap(Collection::stream).collect(Collectors.toList());

        return new ForkJoinGraph(allActive, edges, root);
    }

    // #endregion

    // #region Parallelism Metrics
    /**
     * Structural work: number of segments.
     * @return Structural work.
     */
    public double structuralWork() {
        return Math.max(1, _segments.size());
    }

    /**
     * Empirical work: sum of segment durations.
     * @return Empirical work in nanoseconds.
     */
    public double empiricalWork() {
        return _segments.stream().map(ForkJoinSegment::getNanoSeconds).reduce(Long::sum).orElse(0L);
    }

    /**
     * Structural critical path length: longest path in graph by number of segments.
     * @return
     */
    public double structuralCriticalPath() {
        if (_segments.isEmpty())
            return 1;

        var topo = topoOrder();
        var succ = successorsMap();

        Map<ForkJoinSegment, Integer> dist = new HashMap<>();
        int best = 0;
        for (ForkJoinSegment u : topo) {
            int du = Math.max(dist.getOrDefault(u, 0), 1); // unit weight
            for (ForkJoinSegment v : succ.getOrDefault(u, List.of())) {
                dist.put(v, Math.max(dist.getOrDefault(v, 0), du + 1));
            }
            best = Math.max(best, du);
        }
        return best > 0 ? best : 1;
    }

    /**
     * Empirical critical path length: longest path in graph by segment durations.
     * @return
     */
    public double empiricalCriticalPath() {
        if (_segments.isEmpty())
            return 0.0;

        var topo = topoOrder();
        var succ = successorsMap();

        Map<ForkJoinSegment, Integer> dist = new HashMap<>();
        int best = 0;

        for (ForkJoinSegment u : topo) {
            int du = dist.getOrDefault(u, 0);
            for (ForkJoinSegment v : succ.getOrDefault(u, List.of())) {
                dist.put(v, Math.max(dist.getOrDefault(v, 0), du + (int) u.getNanoSeconds()));
            }
            best = Math.max(best, du + (int) u.getNanoSeconds());
        }

        return best > 0 ? best : 1;
    }

    /**
     * Structural speedup: structural work / structural critical path.
     * @return
     */
    public double structuralSpeedup() {
        return structuralWork() / structuralCriticalPath();
    }

    /**
     * Empirical speedup: empirical work / empirical critical path.
     * @return
     */
    public double empiricalSpeedup() {
        return empiricalWork() / empiricalCriticalPath();
    }

    /**
     * Final segment count: number of segments with no outgoing edges.
     * @return 1 if everything is correct and all forked tasks were joined. Something greater than 1 indicates lost parallelism.
     */
    public int finalSegmentCount() {
        // Number of segments with no outgoing edges
        var dests = _edges.stream().map(ForkJoinEdge::start).collect(Collectors.toSet());
        return (int) _segments.stream().filter(s -> !dests.contains(s)).count();
    }

    // #endregion

    // #region DOT Graph Output

    /**
     * Convert to DOT format for visualization.
     * @return A fancy formatted DOT string.
     */
    public String toDOT() {
        return toDOT(true);
    }

    /**
     * Convert to DOT format for visualization.
     * @param fancy Whether to use fancy formatting.
     * @return A DOT string.
     */
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
                    : switch (e.type()) {
                        case SEQUENTIAL -> "black";
                        case FORK -> "green";
                        case JOIN -> "blue";
                        case COMPUTE -> "red";
                        case RETURN -> "orange";
                    };
            sb.append("  \"").append(e.start()).append("\" -> \"").append(e.end()).append("\" [color=").append(color)
                    .append("];\n");
        }

        sb.append("}");
        return sb.toString();
    }

    // #endregion

    // #region Private Graph Helpers
    private Map<ForkJoinSegment, List<ForkJoinSegment>> successorsMap() {
        final var map = new HashMap<ForkJoinSegment, List<ForkJoinSegment>>();
        for (ForkJoinEdge e : _edges)
            map.computeIfAbsent(e.start(), k -> new ArrayList<>()).add(e.end());

        return map;
    }

    private List<ForkJoinSegment> topoOrder() {
        // DFS from root over edges
        final var order = new ArrayList<ForkJoinSegment>();
        final var seen = new HashSet<ForkJoinSegment>();
        dfs(_root, seen, order);
        Collections.reverse(order);
        return order;
    }

    private void dfs(ForkJoinSegment u, Set<ForkJoinSegment> seen, List<ForkJoinSegment> out) {
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

    private static Map<Long, List<ForkJoinSegment>> buildActiveSegments(Map<Long, List<ForkJoinEvent>> perTask,
            Map<Long, List<long[]>> waitIntervals) {
        Map<Long, List<ForkJoinSegment>> active = new HashMap<>();
        for (var entry : perTask.entrySet()) {
            long taskId = entry.getKey();
            var evts = entry.getValue();
            var waits = waitIntervals.get(taskId);

            var segs = new ArrayList<ForkJoinSegment>();
            for (int i = 0; i + 1 < evts.size(); i++) {
                ForkJoinEvent a = evts.get(i), b = evts.get(i + 1);
                long t0 = a.getTimestamp(), t1 = b.getTimestamp();
                if (t1 <= t0)
                    continue; // zero/negative -> ignore
                if (withinAny(t0, t1, waits))
                    continue; // skip wait time
                segs.add(new ForkJoinSegment(a, b, segs.size()));
            }
            if (!segs.isEmpty())
                active.put(taskId, segs);
        }
        return active;
    }

    private static void addSequentialEdges(Map<Long, List<ForkJoinSegment>> activeByTask, List<ForkJoinEdge> edges) {
        for (var entry : activeByTask.entrySet()) {
            var segs = entry.getValue();
            for (int i = 0; i + 1 < segs.size(); i++) {
                edges.add(new ForkJoinEdge(segs.get(i), segs.get(i + 1), ForkJoinEdge.Type.SEQUENTIAL));
            }
        }
    }

    private static void addCausalEdges(List<ForkJoinEvent> events,
            Map<Long, List<ForkJoinSegment>> activeByTask,
            List<ForkJoinEdge> edges) {
        for (var e : events) {
            if (e instanceof ForkJoinEvent.ForkEvent fe) {
                var p = lastActiveEndingAtOrBefore(activeByTask.get(fe.getTaskId()), fe.getTimestamp());
                var c = firstActive(activeByTask.get(fe.getChildId()));
                if (p != null && c != null)
                    edges.add(new ForkJoinEdge(p, c, ForkJoinEdge.Type.FORK));

            } else if (e instanceof ForkJoinEvent.JoinEvent je) {
                var cLast = lastActive(activeByTask.get(je.getChildId()));
                var pNext = firstActiveStartingAtOrAfter(activeByTask.get(je.getTaskId()), je.getTimestamp());
                if (cLast != null && pNext != null)
                    edges.add(new ForkJoinEdge(cLast, pNext, ForkJoinEdge.Type.JOIN));

            } else if (e instanceof ForkJoinEvent.ComputeEvent ce) {
                var p = lastActiveEndingAtOrBefore(activeByTask.get(ce.getTaskId()), ce.getTimestamp());
                var cFirst = firstActive(activeByTask.get(ce.getChildId()));
                if (p != null && cFirst != null)
                    edges.add(new ForkJoinEdge(p, cFirst, ForkJoinEdge.Type.COMPUTE));

            } else if (e instanceof ForkJoinEvent.ComputeFinishedEvent cfe) {
                var cLast = lastActive(activeByTask.get(cfe.getChildId()));
                var pNext = firstActiveStartingAtOrAfter(activeByTask.get(cfe.getTaskId()), cfe.getTimestamp());
                if (cLast != null && pNext != null)
                    edges.add(new ForkJoinEdge(cLast, pNext, ForkJoinEdge.Type.RETURN));
            }
        }
    }

    private static ForkJoinSegment firstActive(List<ForkJoinSegment> segs) {
        if (segs == null || segs.isEmpty())
            return null;
        return segs.getFirst();
    }

    private static ForkJoinSegment lastActive(List<ForkJoinSegment> segs) {
        if (segs == null || segs.isEmpty())
            return null;
        return segs.getLast();
    }

    private static ForkJoinSegment lastActiveEndingAtOrBefore(List<ForkJoinSegment> segs, long ts) {
        if (segs == null)
            return null;
        ForkJoinSegment cand = null;
        for (var s : segs) {
            if (s.getEndTime() <= ts)
                cand = s;
            else
                break;
        }
        return cand;
    }

    private static ForkJoinSegment firstActiveStartingAtOrAfter(List<ForkJoinSegment> segs, long ts) {
        if (segs == null)
            return null;
        for (var s : segs) {
            if (s.getStartTime() >= ts)
                return s;
        }
        return null;
    }
    // #endregion
}
