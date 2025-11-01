package edu.washington.cse332.autograder.concurrent;

/**
 * Imitates a real ForkJoinPool.
 * See <a href=
 * "https://docs.oracle.com/en/java/javase/25/docs/api//java.base/java/util/concurrent/ForkJoinPool.html">Oracle
 * Docs</a> for entire API spec.
 * "invoke" and "execute" function as expected.
 * Various methods are replicated here, and marked as deprecated.
 * 
 * @author Albert Du
 */
public class ForkJoinPool {
    public ForkJoinPool() {
    }

    public ForkJoinPool(int parallelism) {
    }

    public static ForkJoinPool commonPool() {
        return new ForkJoinPool();
    }

    public <T> T invoke(InstrumentedTask<T> task) {
        return task.execute();
    }
}
