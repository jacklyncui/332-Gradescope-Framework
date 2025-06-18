package edu.washington.cse332.autograder;

import edu.washington.cse332.autograder.config.TestConfig;
import edu.washington.cse332.autograder.config.Visibility;

import java.io.FileOutputStream;
import java.io.PrintStream;

public class TestBundle {
    /**
     * <p>Defines the behavior of testcases</p>
     */
    public final TestConfig config;

    /**
     * <p>The "sum" of all grade strings in JSON <b>with</b> partial scores</p>
     */
    private String allGradesStringWithPartialScores = "";

    /**
     * <p>The "sum" of all grade strings in JSON <b>without</b> partial scores</p>
     */
    private String allGradesStringNoPartialFailedTests = "";


    /**
     * <p>Indicates if all tests are passed</p>
     */
    protected boolean allPassed = true;

    /**
     * <p>The possible points in this test bundle.</p>
     */
    private int sumScore = 0;

    /**
     * <p>The output flow for autograder JSONS</p>
     */
    private final PrintStream console;

    /**
     * <p>The output flow for autograder to CONSOLE</p>
     */
    private PrintStream debugOutput;

    /**
     * The constructor of TestBundle
     *
     * @param scoreWhenAllPassed indicates if student can receive partial credits
     * @param testVisibility     indicates the visibility of the test suite
     * @param suiteName          indicates the testcase bundle name
     */
    public TestBundle(boolean scoreWhenAllPassed, Visibility testVisibility, String suiteName, PrintStream console) {
        this.config = new TestConfig(scoreWhenAllPassed, testVisibility, suiteName);
        this.console = console;
        try {
            PrintStream resultFile = new PrintStream("printed.txt");
            System.setOut(resultFile);
            debugOutput = new PrintStream(new FileOutputStream("debugInfo.txt", false));
        } catch (Exception e) {
        }
        runTests();
        printResults();
    }

    /**
     * <p>This method should be inherited and all test logic should go inside here</p>
     */
    protected void runTests() {
    }

    protected final void addGradeString(int score, int maxscore, String testname, String message) {
        String partialGrade = "{\n" +
                "    \"score\": " + score + ",\n" +
                "    \"maxscore\": " + maxscore + ",\n" +
                "    \"status\": \"" + (score == maxscore ? "passed" : "failed") + "\",\n" +
                "    \"name\": \"" + config.getSuiteName() + " - " + testname + "\",\n" +
                "    \"output\": \"" + message + "\",\n" +
                "    \"visibility\": \"" + config.getTestVisibility().name() + "\"\n" +
                "},";

        String noPartialGrade = "{\n" +
                "    \"score\": 0,\n" +
                "    \"maxscore\": 0,\n" +
                "    \"status\": \" failed\",\n" +
                "    \"name\": \"" + config.getSuiteName() + " - " + testname + "\",\n" +
                "    \"output\": \"" + message + "\",\n" +
                "    \"visibility\": \"" + config.getTestVisibility().name() + "\"\n" +
                "},";

        allPassed &= (score == maxscore);

        sumScore += score;
        allGradesStringWithPartialScores += partialGrade;

        if (score != maxscore) {
            allGradesStringNoPartialFailedTests += noPartialGrade;
        }
    }

    /**
     * <p>Print the result string in JSON format to the <code>printed.txt</code> file.</p>
     */
    protected final void printResults() {
        if (allPassed) {
            // All tests passed
            String grade = "{\n" +
                    "    \"score\": " + sumScore + ",\n" +
                    "    \"maxscore\": " + sumScore + ",\n" +
                    "    \"status\": \"passed\",\n" +
                    "    \"name\": \"" + config.getSuiteName() + " - All Tests\",\n" +
                    "    \"output\": \"Passed!\",\n" +
                    "    \"visibility\": \"" + config.getTestVisibility().name() + "\"\n" +
                    "},";
            console.println(grade);
        } else if (config.isScoreWhenAllPassed()) {
            // Not all tests passed and the config is set to ALLOW partial scoring
            console.println(allGradesStringWithPartialScores);
        } else {
            // Not all tests passed and the config is set to NO partial scoring
            console.println(allGradesStringNoPartialFailedTests);
        }
    }

    /**
     * <p>Add information to the debug output flow</p>
     */
    public final void addDebugInfo(String info) {
        if (debugOutput != null) {
            debugOutput.println(info);
        }
    }
}