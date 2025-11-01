package edu.washington.cse332.autograder;

import java.util.Base64;

/**
 * Utility class for rendering GraphViz DOT source code to images.
 * 
 * @author Albert Du
 */
public class GraphViz {

    /**
     * Renders the given DOT source code to an HTML tag with an image.
     * @param dotSource The DOT source code to render.
     * @return An HTML img tag containing the rendered image.
     */
    public static String renderDOTToHTML(String dotSource) {
        String base64Image = renderDOTToBase64(dotSource);
        return "<img src=\"" + base64Image + "\" />";
    }

    /**
     * Renders the given DOT source code to an SVG image in base64.
     * Ready for use in an img tag.
     * @param dotSource The DOT source code representing the graph.
     * @return A base64 webp representation of the graph as a String.
     */
    private static String renderDOTToBase64(String dotSource) {
        try {
            var pb = new ProcessBuilder("dot", "-Twebp", "-Gsize=20,20\\!", "-Gdpi=100");
            var process = pb.start();

            // Write DOT source to the process's stdin
            try (var outStream = process.getOutputStream()) {
                outStream.write(dotSource.getBytes());
            }

            byte[] bytes;
            
            // Read SVG output from the process's stdout
            try (var inStream = process.getInputStream()) {
                bytes = inStream.readAllBytes();
            }
            
            var base64 = Base64.getEncoder().encodeToString(bytes);
            var webpOutput = "data:image/webp;base64," + base64;

            // Wait for the process to complete
            var exitCode = process.waitFor();

            if (exitCode == 127)
                throw new AutograderException(
                        "GraphViz 'dot' command not found. Please ensure GraphViz is installed and 'dot' is in your system PATH.");

            if (exitCode != 0)
                throw new AutograderException("GraphViz 'dot' command failed with exit code " + exitCode);

            return webpOutput;
        } catch (Exception e) {
            throw new AutograderException(e);
        }
    }
}