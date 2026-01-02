package edu.washington.cse332.autograder;

import java.io.PrintStream;

public class Debug {
    public static void addDebugLine(String line) {
        try(PrintStream debugStream = new PrintStream(new FileOutputStream("debugInfo.txt", true))) {
            debugStream.println(line);
        } catch (Exception ignored) {
        }
    }
}
