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
    public static ForkJoinAnalyzer shared() {
        return sharedLocal.get();
    }

    // Fields:
    private long taskCount;
    private long executingTask;
    private final Map<String, Long> perClassCount = new HashMap<>();
    private final List<ForkJoinEvent> eventLog = new ArrayList<>();

    /**
     * Resets the analyzer's state. Call this before fork join timings are desired
     * or after to clean up.
     */
    private void reset() {
        taskCount = 0;
        executingTask = -1;
        perClassCount.clear();
        eventLog.clear();
    }

    /**
     * Conduct an analysis of recorded events.
     *
     * @return Results, including graph information and task counts.
     */
    private ParallelismResult analyze() {
        return new ParallelismResult(ForkJoinGraph.create(eventLog), taskCount, Map.copyOf(perClassCount));
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
        final var cname = task.getClass().getName();

        if (perClassCount.containsKey(cname))
            perClassCount.put(cname, perClassCount.get(cname) + 1);
        else
            perClassCount.put(cname, 1L);

        return taskCount++;
    }

    /**
     * For {@link InstrumentedTask}
     *
     * @param event to be recorded
     */
    void log(ForkJoinEvent event) {
        eventLog.add(event);
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
