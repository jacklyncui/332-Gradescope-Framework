package edu.washington.cse332.autograder.concurrent;

/**
 * This is equivalent to
 * {@link java.util.concurrent.RecursiveAction}
 * that is capable of counting and analyzing calls to {@link #fork()},
 * {@link #join()}, and
 * {@link #compute()}.
 * This is intended to be used with {@link InstrumentedForkJoinPool} although
 * not strictly required.
 *
 * @author Albert Du
 */
public abstract class RecursiveAction extends InstrumentedTask<Void> {
    /**
     * We rename the compute definition in the student submission to
     * {@link #__impl_compute()}
     * Leaving the body alone lets us log recursive calls to {@link #compute()}.
     */
    protected abstract void __impl_compute();

    /**
     * Recursively called from {@link #__impl_compute()}.
     */
    public final void compute() {
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
