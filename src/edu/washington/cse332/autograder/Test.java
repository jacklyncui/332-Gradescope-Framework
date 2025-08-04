package edu.washington.cse332.autograder;

import edu.washington.cse332.autograder.config.Visibility;

import java.lang.annotation.*;

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

}