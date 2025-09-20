# CSE 332 AutoGrader Infrastructure

This is the infrastructure for the CSE 332 Gradescope Autograder, aiming to streamline the grading process for
programming assignments in the CSE 332 course with modern Java features like Reflections and Annotations.

## Usage
To use this autograder infrastructure, you need to write your own tests and annotate them with the provided annotations.
The autograder will automatically discover and run these tests when grading student submissions with Reflections.

Below is a brief overview of the annotations provided:
- `@Test`: Marks a method as a test case.
- `@TestSuite`: Marks a class as a test suite containing multiple test cases.

Meanwhile, you will need to use shell scripts to compile and run the autograder. The students' submissions will be
passed as command-line arguments to the autograder.
```bash
javac -cp ".:/path/to/framework.jar" path/to/test/File.java
java -cp ".:/path/to/framework.jar" edu.washington.cse332.autograder.TestRunner path/to/test/File
```

## Acknowledgements
This project is heavily based on the autograder developed by
[Nathan Brunelle](https://www.cs.washington.edu/people/faculty/nathan-brunelle/) in the 2024 Summer. Meanwhile,
[Jacklyn Cui](https://jcui.notion.site/) has rewritten the autograder to support modern Java features (Annotation and
Reflection) in 2025 Summer for better maintainability and extensibility, as well as easier writing of tests rather
than writing a lot of boilerplate code.

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.