package edu.washington.cse332.autograder;

import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * <p>A utility class for logging debug information to a file.</p>
 *
 * @author Jacklyn Cui
 */

public class Debug {
    public static void addDebugLine(String line) {
        try(PrintStream debugStream = new PrintStream(new FileOutputStream("debugInfo.txt", true))) {
            debugStream.println(line);
        } catch (Exception ignored) {
        }
    }
}
