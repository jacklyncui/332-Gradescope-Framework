package edu.washington.cse332.autograder.config;

/**
 * <p>Enum representing the format of test output.</p>
 *
 * @author Albert Du
 */
public enum TestOutputFormat {
    HTML,
    TEXT;

    @Override
    public String toString() {
        return switch (this) {
            case HTML -> "html";
            case TEXT -> "text";
        };
    }
}
