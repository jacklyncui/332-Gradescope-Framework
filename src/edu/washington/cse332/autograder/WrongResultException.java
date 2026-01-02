package edu.washington.cse332.autograder;

import edu.washington.cse332.autograder.config.TestOutputFormat;

/**
 * <p>An exception indicating that the test produced an incorrect result.</p>
 * 
 * <p>Immediately terminates the test execution when thrown.</p>
 *
 * @author Jacklyn Cui
 */
public class WrongResultException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private TestOutputFormat outputFormat = TestOutputFormat.TEXT;

    /**
     * <p>Constructs a new WrongResultException with the specified detail message.</p>
     *
     * @param message the detail message
     */
    public WrongResultException(String message) {
        super(message);
    }

    /**
     * <p>Constructs a new WrongResultException with the specified detail message and output format.</p>
     *
     * @param message the detail message
     * @param format the output format
     */
    public WrongResultException(String message, TestOutputFormat format) {
        super(message);
        this.outputFormat = format;
    }

    /**
     * <p>Constructs a new WrongResultException with the specified cause.</p>
     *
     * @param cause the cause of the exception
     */
    public WrongResultException(Throwable cause) {
        super(cause);
    }

    /**
     * <p>Constructs a new WrongResultException with the specified detail message and cause.</p>
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public WrongResultException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * <p>Gets the output format associated with this exception.</p>
     * @return the output format
     */
    TestOutputFormat getOutputFormat() {
        return outputFormat;
    }
}
