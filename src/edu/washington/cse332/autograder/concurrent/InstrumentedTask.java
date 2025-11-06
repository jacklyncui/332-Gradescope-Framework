package edu.washington.cse332.autograder.concurrent;

/**
 * A version of {@link java.util.concurrent.ForkJoinTask} with event recording.
 * 
 * @param <T> output type, if applicable
 * @author Albert Du
 */
public abstract class InstrumentedTask<T> {
    // #region private fields
    private final ForkJoinAnalyzer analyzer = ForkJoinAnalyzer.shared();
    private final long parentId = analyzer.getExecutingTaskId();
    private final long taskId = analyzer.makeId(this);
    private boolean computed = false;
    private boolean forked = false;
    private boolean joined = false;
    private T result = null;
    // #endregion

    // #region Simulated functionality
    public final T join() {
        if (joined)
            return result;

        if (!forked)
            throw new BadParallelismException("Task has not been forked. Do not call join.");

        if (computed)
            throw new BadParallelismException("Task has already been computed. Do not call join.");

        joined = true;

        result = execute();
        logJoin();

        return result;
    }

    public final InstrumentedTask<T> fork() {
        if (forked)
            throw new BadParallelismException("Task has already been forked. Do not fork again.");

        if (computed)
            throw new BadParallelismException("Task has already been computed. Do not call fork.");

        forked = true;

        logFork();
        return this;
    }
    // #endregion

    protected abstract T execute();

    /**
     * Exclusively called from ForkJoinPool to start execution of this task.
     * 
     * @return
     */
    T spawn() {
        var realParentId = analyzer.getExecutingTaskId();
        if (realParentId != -1)
            // we are contextually inside another task
            analyzer.log(new ForkJoinEvent.ComputeEvent(realParentId, taskId));

        var result = execute();

        if (realParentId != -1)
            analyzer.log(new ForkJoinEvent.ComputeFinishedEvent(realParentId, taskId));

        return result;
    }

    // #region Event Loggers
    protected final void logCompute() {
        if (forked)
            throw new BadParallelismException("Task has already been forked. Do not call compute.");

        if (joined)
            throw new BadParallelismException("Task has already been joined. Do not call compute.");

        if (computed)
            throw new BadParallelismException("Task has already been computed. Do not call compute again.");

        computed = true;

        analyzer.log(new ForkJoinEvent.ComputeEvent(parentId, taskId));
    }

    protected final void logComputeFinished() {
        analyzer.log(new ForkJoinEvent.ComputeFinishedEvent(parentId, taskId));
    }

    protected final void logEnter() {
        analyzer.setExecutingTaskId(taskId);
        analyzer.log(new ForkJoinEvent.EnterEvent(taskId));
    }

    protected final void logExit() {
        analyzer.log(new ForkJoinEvent.ExitEvent(taskId));
        analyzer.setExecutingTaskId(parentId);
    }

    protected final void logFork() {
        analyzer.log(new ForkJoinEvent.ForkEvent(parentId, taskId));
    }

    protected final void logJoin() {
        analyzer.log(new ForkJoinEvent.JoinEvent(parentId, taskId));
    }

    // #endregion
}
