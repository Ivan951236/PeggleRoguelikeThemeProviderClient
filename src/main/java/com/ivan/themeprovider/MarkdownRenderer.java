package com.ivan.themeprovider;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Service for rendering Markdown files to HTML
 */
public class MarkdownRenderer {
    private static final Logger logger = LoggerFactory.getLogger(MarkdownRenderer.class);
    
    private final Parser parser;
    private final HtmlRenderer renderer;
    
    public MarkdownRenderer() {
        // Configure markdown options
        MutableDataSet options = new MutableDataSet();
        
        // Enable GitHub Flavored Markdown extensions
        options.set(Parser.EXTENSIONS, Arrays.asList(
            TablesExtension.create(),
            AutolinkExtension.create(),
            StrikethroughExtension.create(),
            TaskListExtension.create()
        ));
        
        // Create parser and renderer
        parser = Parser.builder(options).build();
        renderer = HtmlRenderer.builder(options).build();
        
        logger.debug("MarkdownRenderer initialized with GitHub Flavored Markdown support");
    }
    
    /**
     * Render markdown file to HTML
     * 
     * @param markdownFile Path to the markdown file
     * @return HTML string or null if rendering failed
     */
    public String renderFile(Path markdownFile) {
        if (!Files.exists(markdownFile)) {
            logger.warn("Markdown file does not exist: {}", markdownFile);
            return null;
        }
        
        try {
            String markdownContent = Files.readString(markdownFile);
            return renderString(markdownContent);
        } catch (IOException e) {
            logger.error("Failed to read markdown file: {}", markdownFile, e);
            return null;
        }
    }
    
    /**
     * Render markdown string to HTML
     * 
     * @param markdownContent Markdown content as string
     * @return HTML string
     */
    public String renderString(String markdownContent) {
        if (markdownContent == null || markdownContent.trim().isEmpty()) {
            return "";
        }
        
        try {
            Node document = parser.parse(markdownContent);
            String html = renderer.render(document);
            
            logger.debug("Successfully rendered {} characters of markdown to {} characters of HTML", 
                        markdownContent.length(), html.length());
            
            return html;
        } catch (Exception e) {
            logger.error("Failed to render markdown content", e);
            return "<p>Error rendering markdown content: " + e.getMessage() + "</p>";
        }
    }
    
    /**
     * Create a complete HTML document with CSS styling
     * 
     * @param htmlContent The HTML content from markdown rendering
     * @param title The document title
     * @param darkMode Whether to use dark mode styling
     * @return Complete HTML document
     */
    public String createHtmlDocument(String htmlContent, String title, boolean darkMode) {
        String cssStyles = getCssStyles(darkMode);
        
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s</title>
                <style>%s</style>
            </head>
            <body>
                <div class="markdown-body">
                    %s
                </div>
            </body>
            </html>
            """.formatted(title, cssStyles, htmlContent);
    }
    
    /**
     * Get CSS styles for markdown rendering
     */
    private String getCssStyles(boolean darkMode) {
        if (darkMode) {
            return """
                .markdown-body {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Helvetica, Arial, sans-serif;
                    font-size: 14px;
                    line-height: 1.5;
                    color: #e6edf3;
                    background-color: #0d1117;
                    padding: 16px;
                    max-width: 100%;
                    box-sizing: border-box;
                }
                .markdown-body h1, .markdown-body h2, .markdown-body h3, .markdown-body h4, .markdown-body h5, .markdown-body h6 {
                    color: #f0f6fc;
                    border-bottom: 1px solid #21262d;
                    padding-bottom: 0.3em;
                    margin-top: 24px;
                    margin-bottom: 16px;
                }
                .markdown-body p {
                    margin-top: 0;
                    margin-bottom: 16px;
                }
                .markdown-body a {
                    color: #58a6ff;
                    text-decoration: none;
                }
                .markdown-body a:hover {
                    text-decoration: underline;
                }
                .markdown-body code {
                    background-color: #161b22;
                    color: #e6edf3;
                    padding: 2px 4px;
                    border-radius: 3px;
                    font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
                }
                .markdown-body pre {
                    background-color: #161b22;
                    border-radius: 6px;
                    padding: 16px;
                    overflow: auto;
                }
                .markdown-body blockquote {
                    border-left: 4px solid #21262d;
                    padding-left: 16px;
                    margin-left: 0;
                    color: #8b949e;
                }
                .markdown-body table {
                    border-collapse: collapse;
                    width: 100%;
                    margin-bottom: 16px;
                }
                .markdown-body th, .markdown-body td {
                    border: 1px solid #21262d;
                    padding: 8px 12px;
                    text-align: left;
                }
                .markdown-body th {
                    background-color: #161b22;
                    font-weight: bold;
                }
                """;
        } else {
            return """
                .markdown-body {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Helvetica, Arial, sans-serif;
                    font-size: 14px;
                    line-height: 1.5;
                    color: #24292f;
                    background-color: #ffffff;
                    padding: 16px;
                    max-width: 100%;
                    box-sizing: border-box;
                }
                .markdown-body h1, .markdown-body h2, .markdown-body h3, .markdown-body h4, .markdown-body h5, .markdown-body h6 {
                    color: #24292f;
                    border-bottom: 1px solid #d1d9e0;
                    padding-bottom: 0.3em;
                    margin-top: 24px;
                    margin-bottom: 16px;
                }
                .markdown-body p {
                    margin-top: 0;
                    margin-bottom: 16px;
                }
                .markdown-body a {
                    color: #0969da;
                    text-decoration: none;
                }
                .markdown-body a:hover {
                    text-decoration: underline;
                }
                .markdown-body code {
                    background-color: #f6f8fa;
                    color: #24292f;
                    padding: 2px 4px;
                    border-radius: 3px;
                    font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
                }
                .markdown-body pre {
                    background-color: #f6f8fa;
                    border-radius: 6px;
                    padding: 16px;
                    overflow: auto;
                }
                .markdown-body blockquote {
                    border-left: 4px solid #d1d9e0;
                    padding-left: 16px;
                    margin-left: 0;
                    color: #656d76;
                }
                .markdown-body table {
                    border-collapse: collapse;
                    width: 100%;
                    margin-bottom: 16px;
                }
                .markdown-body th, .markdown-body td {
                    border: 1px solid #d1d9e0;
                    padding: 8px 12px;
                    text-align: left;
                }
                .markdown-body th {
                    background-color: #f6f8fa;
                    font-weight: bold;
                }
                """;
        }
    }
    
    /**
     * Check if a file has a markdown extension
     */
    public static boolean isMarkdownFile(Path filePath) {
        String fileName = filePath.getFileName().toString().toLowerCase();
        return fileName.endsWith(".md") || fileName.endsWith(".markdown");
    }
    
    /**
     * Find markdown files in the same directory as a theme file
     * 
     * @param themeFilePath Path to the theme file
     * @return Path to markdown file or null if not found
     */
    public static Path findMarkdownForTheme(Path themeFilePath) {
        if (!Files.exists(themeFilePath)) {
            return null;
        }
        
        Path themeDir = themeFilePath.getParent();
        if (themeDir == null) {
            return null;
        }
        
        String themeBaseName = getBaseName(themeFilePath);
        
        try {
            return Files.list(themeDir)
                .filter(Files::isRegularFile)
                .filter(MarkdownRenderer::isMarkdownFile)
                .filter(path -> getBaseName(path).equals(themeBaseName))
                .findFirst()
                .orElse(null);
        } catch (IOException e) {
            logger.warn("Failed to list files in directory: {}", themeDir, e);
            return null;
        }
    }
    
    /**
     * Get the base name of a file without extension
     */
    private static String getBaseName(Path filePath) {
        String fileName = filePath.getFileName().toString();
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(0, lastDotIndex) : fileName;
    }
}