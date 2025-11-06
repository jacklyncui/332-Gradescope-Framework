package edu.washington.cse332.autograder.concurrent;

/**
 * This is equivalent to
 * {@link java.util.concurrent.RecursiveTask}
 * that is
 * capable of counting and analyzing calls to {@link #fork()}, {@link #join()}, and
 * {@link #compute()}.
 * This must be used with {@link InstrumentedForkJoinPool}.
 *
 * @param <T>
 * @author Albert Du
 */
public abstract class RecursiveTask<T> extends InstrumentedTask<T> {
    /**
     * We rename the compute definition in the student submission to
     * {@link #__impl_compute()}
     * Leaving the body alone lets us log recursive calls to {@link #compute()}.
     *
     * @return T result.
     */
    protected abstract T __impl_compute();

    /**
     * Recursively called from {@link #__impl_compute()}.
     * 
     * @return T result.
     */
    public final T compute() {
        logCompute();
        var value = execute();
        logComputeFinished();
        return value;
    }

    @Override
    protected final T execute() {
        logEnter();
        T result = __impl_compute();
        logExit();
        return result;
    }
}
