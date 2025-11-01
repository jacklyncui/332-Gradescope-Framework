package edu.washington.cse332.autograder.concurrent;

/**
 * Imitates a real {@link java.util.concurrent.ForkJoinPool}.
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
