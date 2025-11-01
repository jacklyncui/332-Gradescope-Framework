package edu.washington.cse332.autograder.concurrent;

/**
 * This is equivalent to
 * `java.util.concurrent.RecursiveAction`
 * that is
 * capable of counting and analyzing calls to `fork`, `join`, and `compute`
 * This must be used with `InstrumentedForkJoinPool`.
 *
 * @author Albert Du
 */
public abstract class RecursiveAction extends InstrumentedTask<Void> {
    /**
     * We rename the compute definition in the student submission to
     * __impl_compute()
     * Leaving the body alone lets us log recursive calls to compute().
     */
    protected abstract void __impl_compute();

    /**
     * Recursively called from __impl_compute().
     */
    protected final void compute() {
        logCompute();
        execute();
        logComputeFinished();
    }

    @Override
    protected final Void execute() {
        logEnter();
        __impl_compute();
        logExit();
        return null;
    }
}
