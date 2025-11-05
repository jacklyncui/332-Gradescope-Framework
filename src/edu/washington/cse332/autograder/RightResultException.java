package edu.washington.cse332.autograder;

import edu.washington.cse332.autograder.config.TestOutputFormat;

/**
 * An exception indicating that the test produced the correct result.
 * 
 * Immediately terminates the test execution when thrown.
 *
 * @author Albert Du
 */
public class RightResultException extends RuntimeException {
    private TestOutputFormat outputFormat = TestOutputFormat.TEXT;

    /**
     * Constructs a new RightResultException with the specified detail message.
     *
     * @param message the detail message
     */
    public RightResultException(String message) {
        super(message);
    }

    /**
     * Constructs a new RightResultException with the specified detail message and output format.
     *
     * @param message the detail message
     * @param format the output format
     */
    public RightResultException(String message, TestOutputFormat format) {
        super(message);
        this.outputFormat = format;
    }


    /**
     * Gets the output format associated with this exception.
     * @return the output format
     */
    TestOutputFormat getOutputFormat() {
        return outputFormat;
    }
}
