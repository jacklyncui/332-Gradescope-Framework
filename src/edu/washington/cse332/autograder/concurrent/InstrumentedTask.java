package edu.washington.cse332.autograder.concurrent;

/**
 * A version of ForkJoinTask with event recording.
 * 
 * @param <T> output type, if applicable
 * @author Albert Du
 */
public abstract class InstrumentedTask<T> {
    // #region private fields
    private final ForkJoinAnalyzer analyzer = ForkJoinAnalyzer.shared();
    private final long parentId = analyzer.getExecutingTaskId();
    private final long taskId = analyzer.makeId(this);
    // #endregion

    // #region Simulated functionality
    public final T join() {
        T result = execute();
        logJoin();

        return result;
    }

    public final InstrumentedTask<T> fork() {
        logFork();
        return this;
    }
    // #endregion

    protected abstract T execute();

    // #region Event Loggers
    protected final void logCompute() {
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
