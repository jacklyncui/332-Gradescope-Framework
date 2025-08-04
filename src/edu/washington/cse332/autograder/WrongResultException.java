package edu.washington.cse332.autograder;

public class WrongResultException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new WrongResultException with the specified detail message.
     *
     * @param message the detail message
     */
    public WrongResultException(String message) {
        super(message);
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
}
