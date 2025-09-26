package edu.washington.cse332.autograder;

import java.io.PrintStream;

public class Debug {
    public static void addDebugLine(String line) {
        try(PrintStream debugStream = new PrintStream("debugInfo.txt")) {
            debugStream.println(line);
        } catch (Exception ignored) {
        }
    }
}
