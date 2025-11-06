package edu.washington.cse332.autograder.concurrent;

import java.util.*;

/**
 * Fork Join Analyzer. Must be used with subtypes of {@link InstrumentedTask}
 * provided in this package.
 * 
 * @author Albert Du
 */
public final class ForkJoinAnalyzer {
    private ForkJoinAnalyzer() {
        reset();
    }

    // Default value for each thread
    private static final ThreadLocal<ForkJoinAnalyzer> sharedLocal = ThreadLocal.withInitial(ForkJoinAnalyzer::new);

    /**
     * Analyze a Runnable that uses Fork Join tasks.
     *
     * @param r Runnable to be analyzed
     * @return ParallelismResult of analysis
     */
    public static ParallelismResult analyze(Runnable r) {
        shared().reset();
        r.run();
        return shared().analyze();
    }

    /**
     * Get the current thread's analyzer. No need to reset if first time using.
     *
     * @return The thread's fork join analyzer, or a new one if it does not exist.
     */
    static ForkJoinAnalyzer shared() {
        return sharedLocal.get();
    }

    // Fields: these should be cleared when reset.
    private long taskCount;
    private long forkCalls;
    private long poolInvokes;
    private long computeCalls;
    private long executingTask;
    private final Map<String, Long> perClassCount = new HashMap<>();
    private final Map<String, Long> perClassForkCalls = new HashMap<>();
    private final Map<String, Long> perClassComputeCalls = new HashMap<>();
    private final Map<Long, String> idToClassName = new HashMap<>();
    private final List<ForkJoinEvent> eventLog = new ArrayList<>();

    /**
     * Resets the analyzer's state. Call this before fork join timings are desired
     * or after to clean up.
     */
    private void reset() {
        taskCount = 0;
        executingTask = -1;
        forkCalls = 0;
        computeCalls = 0;
        poolInvokes = 0;
        perClassCount.clear();
        perClassForkCalls.clear();
        perClassComputeCalls.clear();
        idToClassName.clear();
        eventLog.clear();
    }

    /**
     * Conduct an analysis of recorded events.
     *
     * @return Results, including graph information and task counts.
     */
    private ParallelismResult analyze() {
        // one compute call is expected to be the initial call
        var computeRatio = computeCalls + forkCalls > 0 ? (double)(computeCalls) / (computeCalls + forkCalls) : 1;
        
        // calculate the per-class compute ratios
        Map<String, Double> perClassComputeRatio = new HashMap<>();
        for (var entry : perClassCount.entrySet()) {
            String cname = entry.getKey();
            long computes = perClassComputeCalls.getOrDefault(cname, 0L);
            long forks = perClassForkCalls.getOrDefault(cname, 0L);
            double ratio = computes + forks > 0 ? (double)(computes) / (computes + forks) : 1;
            perClassComputeRatio.put(cname, ratio);
        }
        return new ParallelismResult(ForkJoinGraph.create(eventLog), taskCount, Map.copyOf(perClassCount), computeRatio, perClassComputeRatio, poolInvokes);
    }

    /**
     * For debugging. Immediately prints the recorded event log to System.out.
     */
    public void dump() {
        for (var event : eventLog)
            System.out.println(event);
    }

    /**
     * For {@link InstrumentedTask}.
     *
     * @return a new Identifier.
     */
    <T> long makeId(InstrumentedTask<T> task) {
        final var cnameTokens = task.getClass().getName().split("\\$");
        final var cname = cnameTokens[cnameTokens.length - 1];

        if (perClassCount.containsKey(cname))
            perClassCount.put(cname, perClassCount.get(cname) + 1);
        else
            perClassCount.put(cname, 1L);

        idToClassName.put(taskCount, cname);

        return taskCount++;
    }

    /**
     * For {@link InstrumentedTask}
     *
     * @param event to be recorded
     */
    void log(ForkJoinEvent event) {
        if (event instanceof ForkJoinEvent.ForkEvent fe) {
            forkCalls++;
            final var cname = idToClassName.get(fe.getChildId());
            if (perClassForkCalls.containsKey(cname))
                perClassForkCalls.put(cname, perClassForkCalls.get(cname) + 1);
            else
                perClassForkCalls.put(cname, 1L);
        }
        else if (event instanceof ForkJoinEvent.ComputeEvent ce) {
            computeCalls++;
            final var cname = idToClassName.get(ce.getChildId());
            if (perClassComputeCalls.containsKey(cname))
                perClassComputeCalls.put(cname, perClassComputeCalls.get(cname) + 1);
            else
                perClassComputeCalls.put(cname, 1L);
        }

        eventLog.add(event);
    }

    void logPoolInvoke() {
        poolInvokes++;
    }

    /**
     * Returns the currently executing Task Identifier.
     *
     * @return the current task
     */
    long getExecutingTaskId() {
        return executingTask;
    }

    /**
     * Set's the currently executing Task Identifier.
     *
     * @param executingId to be set
     */
    void setExecutingTaskId(long executingId) {
        executingTask = executingId;
    }
}
