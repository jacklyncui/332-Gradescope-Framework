package edu.washington.cse332.autograder.config;

/**
 * <p>Configuration of test bundles</p>
 */
public class TestConfig {

    /**
     * <p>Defines if student receives some partial score.</p>
     * <p>If set to true, student will only receive score when all tests in this bundle has passed.
     * Otherwise, student will receive partial points.</p>
     */
    private final boolean scoreWhenAllPassed;

    /**
     * <p>Defines if the test suite is hidden to students.</p>
     */
    private final Visibility testVisibility;

    /**
     * <p>Defines the name of that test bundle.</p>
     */
    private final String suiteName;

    /**
     * <p>Constructor of <code>TestConfig</code> class.</p>
     *
     * @param scoreWhenAllPassed if student can receive partial points
     */
    public TestConfig(boolean scoreWhenAllPassed, Visibility testVisibility, String suiteName) {
        this.scoreWhenAllPassed = scoreWhenAllPassed;
        this.testVisibility = testVisibility;
        this.suiteName = suiteName;
    }

    /**
     * <p>Getter method for <code>scoreWhenAllPassed</code></p>
     *
     * @return <code>scoreWhenAllPassed</code>
     */
    public boolean isScoreWhenAllPassed() {
        return scoreWhenAllPassed;
    }

    /**
     * <p>Getter method for <code>testVisibility</code></p>
     *
     * @return <code>testVisibility</code>
     */
    public Visibility getTestVisibility() {
        return testVisibility;
    }

    /**
     * <p>Getter method for <code>suiteName</code></p>
     *
     * @return <code>suiteName</code>
     */
    public String getSuiteName() {
        return suiteName;
    }
}