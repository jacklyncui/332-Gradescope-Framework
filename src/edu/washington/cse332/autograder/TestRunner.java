package edu.washington.cse332.autograder;

import edu.washington.cse332.autograder.config.TestOutputFormat;
import edu.washington.cse332.autograder.config.Visibility;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

public class TestRunner {

    /**
     * <p>
     * Runs the test suite(s) specified by the fully qualified class names passed as
     * arguments.
     * </p>
     * <p>
     * Each class must be annotated with {@link TestSuite} and contain methods
     * annotated with
     * {@link Test}.
     * </p>
     * <p>
     * Outputs results in JSON format to standard output, and captures any output
     * printed to
     * standard output during test execution in a file named
     * <code>printed.txt</code>.
     * </p>
     * 
     * @param args fully qualified class names of test suites to run
     * @throws Exception if any error occurs during test execution
     */
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: java TestRunner <fully.qualified.TestClassName>");
            System.exit(1);
        }
        for (String cls : args) {
            runSuite(Class.forName(cls));
        }
    }

    /**
     * Runs a single test suite class.
     * 
     * @param suiteClass the class to run, must be annotated with {@link TestSuite}
     * @throws Exception if any error occurs during test execution
     */
    private static void runSuite(Class<?> suiteClass) throws Exception {
        TestSuite suiteAnn = suiteClass.getAnnotation(TestSuite.class);
        if (suiteAnn == null)
            return;

        // Basic info from the suite annotation
        boolean partialCredit = suiteAnn.partialCredit();
        String suiteName = suiteAnn.name();
        Visibility suiteVis = suiteAnn.visibility();

        // Redirect System.out -> printed.txt
        PrintStream origOut = System.out;
        PrintStream studentOut = new PrintStream("printed.txt");
        System.setOut(studentOut);

        // Prepare to collect results
        boolean allPassed = true;
        int totalPossible = 0;
        boolean isSanityCheck = suiteAnn.sanityCheck();
        List<String> jsonEntries = new ArrayList<>();
        List<String> persistentJsonEntries = new ArrayList<>();

        // Instantiate your test class
        Object instance = suiteClass.getDeclaredConstructor().newInstance();

        // Run each @Test
        for (Method m : suiteClass.getDeclaredMethods()) {
            Test testAnn = m.getAnnotation(Test.class);
            if (testAnn == null)
                continue;

            String testName = testAnn.name();
            int points = testAnn.points();
            Visibility vis = testAnn.visibility();
            boolean persistOutput = testAnn.persistOutput();
            totalPossible += points;

            // Clear output buffer
            Output.reset();

            try {
                m.setAccessible(true);
                m.invoke(instance);
                // passed
                jsonEntries.add(makeJson(points, points, suiteName + " - " + testName,
                        Output.getOutput(), Output.getFormat(), vis, isSanityCheck));

                if (persistOutput) {
                    persistentJsonEntries.add(makeJson(0, 0, suiteName + " - " + testName,
                            Output.getOutput(), Output.getFormat(), vis, isSanityCheck));
                }
            } catch (InvocationTargetException ite) {
                Throwable ex = ite.getCause();

                if (ex instanceof RightResultException rre) {
                    // special case: right result but with extra output
                    jsonEntries.add(makeJson(points, points, suiteName + " - " + testName,
                            rre.getMessage(), rre.getOutputFormat(), vis, isSanityCheck));

                    if (persistOutput) {
                        persistentJsonEntries.add(makeJson(0, 0, suiteName + " - " + testName,
                                rre.getMessage(), rre.getOutputFormat(), vis, isSanityCheck));
                    }
                    continue;
                }

                allPassed = false;
                String msg;
                TestOutputFormat format;
                if (ex == null) {
                    msg = "Unknown failure";
                    format = TestOutputFormat.TEXT;
                } else if (ex instanceof WrongResultException wre) {
                    msg = ex.getMessage();
                    format = wre.getOutputFormat();
                } else {
                    msg = ex.getClass().getName() + ": " + ex.getMessage();
                    format = TestOutputFormat.TEXT;
                }
                jsonEntries.add(makeJson(0, points, suiteName + " - " + testName,
                        msg, format, vis, isSanityCheck));
            }
        }

        // Restore System.out, print summary to console
        System.setOut(origOut);

        if (allPassed) {

            // one big “All Tests” entry
            System.out.println(makeJson(totalPossible, totalPossible,
                    suiteName + " - All Tests",
                    "Passed!", TestOutputFormat.TEXT, suiteVis, suiteAnn.sanityCheck()));

            // print the persistent entries right away, they have 0 score so they don't affect the total
            persistentJsonEntries.forEach(System.out::println);

        } else if (partialCredit) {
            jsonEntries.forEach(System.out::println);
        } else {
            // only show the failures
            jsonEntries.stream()
                    .filter(s -> s.contains("\"status\": \"failed\""))
                    .forEach(System.out::println);
        }
    }

    private static String makeJson(int score, int max, String name,
            String output, TestOutputFormat outputFormat, Visibility vis, boolean isSanityCheck) {
        if (isSanityCheck) {
            return "{\n" +
                    "  \"score\": " + 0 + ",\n" +
                    "  \"maxscore\": " + 0 + ",\n" +
                    "  \"status\": \"" + (score == max ? "passed" : "failed") + "\",\n" +
                    "  \"name\": \"" + name + "\",\n" +
                    "  \"output\": \"" + escapeJson(output) + "\",\n" +
                    "  \"output_format\": \"" + outputFormat + "\",\n" +
                    "  \"visibility\": \"" + vis.name() + "\"\n" +
                    "},";
        } else {
            return "{\n" +
                    "  \"score\": " + score + ",\n" +
                    "  \"maxscore\": " + max + ",\n" +
                    "  \"status\": \"" + (score == max ? "passed" : "failed") + "\",\n" +
                    "  \"name\": \"" + name + "\",\n" +
                    "  \"output\": \"" + escapeJson(output) + "\",\n" +
                    "  \"output_format\": \"" + outputFormat + "\",\n" +
                    "  \"visibility\": \"" + vis.name() + "\"\n" +
                    "},";
        }
    }

    private static String escapeJson(String str) {
        return str.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}