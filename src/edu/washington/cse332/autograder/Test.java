package edu.washington.cse332.autograder;

import edu.washington.cse332.autograder.config.Visibility;

import java.lang.annotation.*;

/**
 * <p>Annotation to mark a method as a test case for the autograder.</p>
 *
 * @author Jacklyn Cui
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Test {
    /**
     * <p>The name of the test case.</p>
     * @return the name of the test case
     */
    String name();

    /**
     * <p>The number of points this test case is worth.</p>
     * @return the number of points this test case is worth
     */
    int points();

    /**
     * <p>The visibility of this test case.</p>
     * <p>Defaults to {@link Visibility#visible}.</p>
     * @return the visibility of this test case
     */
    Visibility visibility() default Visibility.visible;

    /**
     * <p>Whether to persist the output of this test case so that the student/grader can view it later.</p>
     * <p>Defaults to false.</p>
     * @return whether to persist the output of this test case
     */
    boolean persistOutput() default false;
}