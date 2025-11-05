package edu.washington.cse332.autograder;

import edu.washington.cse332.autograder.config.TestOutputFormat;

/**
 * A thread-local output holder for test result outputs.
 * 
 * @author Albert Du
 */
public class Output {
    private Output() {
        // prevent instantiation
    }

    private static ThreadLocal<TestOutputFormat> format = ThreadLocal.withInitial(() -> TestOutputFormat.TEXT);
    private static ThreadLocal<String> output = ThreadLocal.withInitial(() -> "Passed");

    public static void set(String out, TestOutputFormat fmt) {
        format.set(fmt);
        output.set(out);
    }

    public static void set(String out) {
        set(out, TestOutputFormat.TEXT);
    }

    /**
     * Resets the output holder to its initial state.
     */
    static void reset() {
        format.set(TestOutputFormat.TEXT);
        output.set("Passed");
    }

    static TestOutputFormat getFormat() {
        return format.get();
    }

    static String getOutput() {
        return output.get();
    }
}
