package edu.washington.cse332.autograder;

import java.util.Base64;

public class GraphViz {

    /**
     * Renders the given DOT source code to an SVG image in base64.
     * Ready for use in an img tag.
     * 
     * @param dotSource The DOT source code representing the graph.
     * @return The SVG representation of the graph as a String.
     * @author Albert Du
     */
    public static String renderDOTToBase64(String dotSource) {
        try {
            var pb = new ProcessBuilder("dot", "-Twebp", "-Gsize=20,20\\!", "-Gdpi=100");
            var process = pb.start();

            // Write DOT source to the process's stdin
            process.getOutputStream().write(dotSource.getBytes());
            process.getOutputStream().close();

            // Read SVG output from the process's stdout
            var inStream = process.getInputStream();
            var bytes = inStream.readAllBytes();
            inStream.close();
            var base64 = Base64.getEncoder().encodeToString(bytes);
            var svgOutput = "data:image/webp;base64," + base64;
            // Wait for the process to complete
            var exitCode = process.waitFor();

            if (exitCode == 127)
                throw new AutograderException(
                        "GraphViz 'dot' command not found. Please ensure GraphViz is installed and 'dot' is in your system PATH.");

            if (exitCode != 0)
                throw new AutograderException("GraphViz 'dot' command failed with exit code " + exitCode);

            return svgOutput;
        } catch (Exception e) {
            throw new AutograderException(e);
        }
    }
}