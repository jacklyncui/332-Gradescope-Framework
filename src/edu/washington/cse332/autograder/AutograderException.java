package edu.washington.cse332.autograder;

/**
 * A custom exception class for the autograder related failures.
 * Wraps other exceptions to provide a unified exception type.
 * 
 * @author Albert Du
 */
public class AutograderException extends RuntimeException {
    public AutograderException(Exception cause) {
        super(cause);
    }

    public AutograderException(String message) {
        super(message);
    }
}
