package edu.washington.cse332.autograder;

import edu.washington.cse332.autograder.config.Visibility;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

public class TestRunner {

    /**
     * <p>Runs the test suite(s) specified by the fully qualified class names passed as arguments.</p>
     * <p>Each class must be annotated with {@link TestSuite} and contain methods annotated with
     * {@link Test}.</p>
     * <p>Outputs results in JSON format to standard output, and captures any output printed to
     * standard output during test execution in a file named <code>printed.txt</code>.</p>
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
     * @param suiteClass the class to run, must be annotated with {@link TestSuite}
     * @throws Exception if any error occurs during test execution
     */
    private static void runSuite(Class<?> suiteClass) throws Exception {
        TestSuite suiteAnn = suiteClass.getAnnotation(TestSuite.class);
        if (suiteAnn == null) return;

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
        List<String> jsonEntries = new ArrayList<>();

        // Instantiate your test class
        Object instance = suiteClass.getDeclaredConstructor().newInstance();

        // Run each @Test
        for (Method m : suiteClass.getDeclaredMethods()) {
            Test testAnn = m.getAnnotation(Test.class);
            if (testAnn == null) continue;

            String testName = testAnn.name();
            int points = testAnn.points();
            Visibility vis = testAnn.visibility();
            totalPossible += points;

            try {
                m.setAccessible(true);
                m.invoke(instance);
                // passed
                jsonEntries.add(makeJson(points, points, suiteName + " - " + testName,
                        "Passed", vis));
            } catch (InvocationTargetException ite) {
                Throwable ex = ite.getCause();
                allPassed = false;
                String msg = ex == null ? "Unknown failure" : ex.getMessage();
                jsonEntries.add(makeJson(0, points, suiteName + " - " + testName,
                        msg.replace("\"","\\\""), vis));
            }
        }

        // Restore System.out, print summary to console
        System.setOut(origOut);

        if (allPassed) {
            // one big “All Tests” entry
            System.out.println(makeJson(totalPossible, totalPossible,
                    suiteName + " - All Tests",
                    "Passed!", suiteVis));
        } else if (partialCredit) {
            jsonEntries.forEach(System.out::println);
        } else {
            // only show the failures
            jsonEntries.stream()
                    .filter(s -> s.contains("\"score\": 0,"))
                    .forEach(System.out::println);
        }
    }

    private static String makeJson(int score, int max, String name,
                                   String output, Visibility vis) {
        return "{\n" +
                "  \"score\": "     + score + ",\n" +
                "  \"maxscore\": "  + max   + ",\n" +
                "  \"status\": \""  + (score==max ? "passed" : "failed") + "\",\n" +
                "  \"name\": \""    + name  + "\",\n" +
                "  \"output\": \""  + output+ "\",\n" +
                "  \"visibility\": \"" + vis.name() + "\"\n" +
                "},";
    }
}