package edu.washington.cse332.autograder;

import edu.washington.cse332.autograder.config.TestOutputFormat;

/**
 * An exception indicating that the test produced an incorrect result.
 * 
 * Immediately terminates the test execution when thrown.
 *
 * @author Jacklyn Cui
 */
public class WrongResultException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private TestOutputFormat outputFormat = TestOutputFormat.TEXT;

    /**
     * Constructs a new WrongResultException with the specified detail message.
     *
     * @param message the detail message
     */
    public WrongResultException(String message) {
        super(message);
    }

    /**
     * Constructs a new WrongResultException with the specified detail message and output format.
     *
     * @param message the detail message
     * @param format the output format
     */
    public WrongResultException(String message, TestOutputFormat format) {
        super(message);
        this.outputFormat = format;
    }

    /**
     * Constructs a new WrongResultException with the specified cause.
     *
     * @param cause the cause of the exception
     */
    public WrongResultException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new WrongResultException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public WrongResultException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Gets the output format associated with this exception.
     * @return the output format
     */
    TestOutputFormat getOutputFormat() {
        return outputFormat;
    }
}
