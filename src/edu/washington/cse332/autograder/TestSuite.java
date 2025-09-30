package edu.washington.cse332.autograder;

import edu.washington.cse332.autograder.config.Visibility;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TestSuite {
    /**
     * <p>The name of the test suite.</p>
     * @return the name of the test suite
     */
    String name();

    /**
     * <p>Indicates if student can receive partial points.</p>
     * <p>If set to true, student will only receive score when all tests in this bundle has passed.
     * Otherwise, student will receive partial points.</p>
     * <p>By default, students WILL earn the partial credits</p>
     * @return if student can receive partial points
     */
    boolean partialCredit() default true;

    /**
     * <p>By default, the visibility of tests in this suite.</p>
     * <p>Defaults to {@link edu.washington.cse332.autograder.config.Visibility#visible}.</p>
     * @return the visibility of this test suite
     */
    Visibility visibility() default Visibility.visible;

    /**
     * <p>Whether this suite is for sanity check</p>
     * <p>If set to true, this suite will not be counted towards the total score.</p>
     * @return if this suite is for sanity check
     */
    boolean sanityCheck() default false;
}