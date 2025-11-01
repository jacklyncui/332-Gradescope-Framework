package edu.washington.cse332.autograder.concurrent;

/**
 * This is equivalent to
 * `java.util.concurrent.RecursiveTask`
 * that is
 * capable of counting and analyzing calls to `fork`, `join`, and `compute`
 * This must be used with `InstrumentedForkJoinPool`.
 *
 * @param <T>
 * @author Albert Du
 */
public abstract class RecursiveTask<T> extends InstrumentedTask<T> {
    /**
     * We rename the compute definition in the student submission to
     * __impl_compute()
     * Leaving the body alone lets us log recursive calls to compute().
     *
     * @return T result.
     */
    protected abstract T __impl_compute();

    /**
     * Recursively called from __impl_compute().
     * 
     * @return T result.
     */
    protected final T compute() {
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
