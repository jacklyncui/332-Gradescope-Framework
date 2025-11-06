package edu.washington.cse332.autograder.config;

/**
 * Enum representing the format of test output.
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
