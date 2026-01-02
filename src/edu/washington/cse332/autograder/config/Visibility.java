package edu.washington.cse332.autograder.config;

/**
 * <p>Enum representing the visibility levels of test cases.</p>
 *
 * <p>See <a href="https://gradescope-autograders.readthedocs.io/en/latest/specs/#controlling-test-case-visibility">
 *     Gradescope Spec</a> for more details</p>
 *
 * @author Jacklyn Cui
 */
public enum Visibility {
    hidden,
    after_due_date,
    after_published,
    visible,
}
