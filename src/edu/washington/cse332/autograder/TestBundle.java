package edu.washington.cse332.autograder;

import edu.washington.cse332.autograder.config.TestConfig;
import edu.washington.cse332.autograder.config.Visibility;

import java.io.File;
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
    private String allGradesString = "";


    /**
     * <p>Indicates if all tests are passed</p>
     */
    protected boolean allPassed = true;

    /**
     * <p>The possible points in this test bundle.</p>
     */
    private int sumScore = 0;


    /**
     * The constructor of TestBundle
     *
     * @param scoreWhenAllPassed indicates if student can receive partial credits
     * @param testVisibility     indicates the visibility of the test suite
     * @param suiteName          indicates the testcase bundle name
     */
    public TestBundle(boolean scoreWhenAllPassed, Visibility testVisibility, String suiteName) {
        this.config = new TestConfig(scoreWhenAllPassed, testVisibility, suiteName);
        try {
            PrintStream resultFile = new PrintStream(new File("printed.txt"));
            System.setOut(resultFile);
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
                "    \"status\": \"" + (score == maxscore ? "passed" : "failed") + "\",\n" +
                "    \"name\": \"" + config.getSuiteName() + " - " + testname + "\",\n" +
                "    \"output\": \"" + message + "\",\n" +
                "    \"visibility\": \"" + config.getTestVisibility().name() + "\"\n" +
                "},";

        sumScore += score;
        allGradesStringWithPartialScores += partialGrade;
        allGradesString += noPartialGrade;
    }

    /**
     * <p>Print the result string in JSON format to the <code>printed.txt</code> file.</p>
     */
    protected final void printResults() {
        if (allPassed) {
            // All tests passed and the config is set to score when all passed
            String grade = "{\n" +
                    "    \"score\": " + sumScore + ",\n" +
                    "    \"maxscore\": " + sumScore + ",\n" +
                    "    \"status\": \"passed\",\n" +
                    "    \"name\": \"" + config.getSuiteName() + " - All Tests\",\n" +
                    "    \"output\": \"Passed!\",\n" +
                    "    \"visibility\": \"" + config.getTestVisibility().name() + "\"\n" +
                    "},";
            System.out.println(grade);
        } else if (config.isScoreWhenAllPassed()) {
            // Not all tests passed and the config is set to score when all passed
            System.out.println(allGradesStringWithPartialScores);
        } else {
            // Not all tests passed and the config is set to allow partial scoring
            System.out.println(allGradesString);
        }
    }
}