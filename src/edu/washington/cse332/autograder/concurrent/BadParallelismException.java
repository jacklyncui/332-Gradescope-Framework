package edu.washington.cse332.autograder.concurrent;

/** 
 * Exception thrown when a parallelism-related error occurs.
 */
public class BadParallelismException extends RuntimeException {
    public BadParallelismException(String message) {
        super(message);
    }
}
