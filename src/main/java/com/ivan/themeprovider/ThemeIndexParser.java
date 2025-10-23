package com.ivan.themeprovider;

import com.ivan.themeprovider.model.ThemeIndex;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.error.YAMLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Parses theme provider index.yml files using SnakeYAML
 */
public class ThemeIndexParser {
    private static final Logger logger = LoggerFactory.getLogger(ThemeIndexParser.class);
    private static final String INDEX_FILE_NAME = "index.yml";
    
    private final Yaml yaml;
    
    public ThemeIndexParser() {
        LoaderOptions loaderOptions = new LoaderOptions();
        this.yaml = new Yaml(loaderOptions);
    }
    
    /**
     * Parse index.yml from a theme provider directory
     * 
     * @param themeProviderDirectory The cloned theme provider directory
     * @return Parsed ThemeIndex or null if parsing fails
     */
    public ThemeIndex parseIndex(Path themeProviderDirectory) {
        Path indexPath = themeProviderDirectory.resolve(INDEX_FILE_NAME);
        
        if (!Files.exists(indexPath)) {
            logger.error("index.yml not found in: {}", themeProviderDirectory);
            return null;
        }
        
        try (FileInputStream fis = new FileInputStream(indexPath.toFile())) {
            return parseIndex(fis);
        } catch (IOException e) {
            logger.error("Failed to read index.yml from: {}", indexPath, e);
            return null;
        }
    }
    
    /**
     * Parse index.yml from an InputStream
     * 
     * @param inputStream The input stream containing YAML data
     * @return Parsed ThemeIndex or null if parsing fails
     */
    public ThemeIndex parseIndex(InputStream inputStream) {
        try {
            // First load as a generic map to handle the complex structure
            @SuppressWarnings("unchecked")
            Map<String, Object> data = yaml.load(inputStream);
            
            if (data == null) {
                logger.error("Empty or invalid YAML content");
                return null;
            }
            
            ThemeIndex themeIndex = new ThemeIndex();
            
            // Parse present_themes with special handling
            Object presentThemesObj = data.get("present_themes");
            if (presentThemesObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> themesMap = (Map<String, Object>) presentThemesObj;
                Map<String, ThemeIndex.ThemeEntry> parsedThemes = parseThemeEntries(themesMap);
                themeIndex.setPresentThemes(parsedThemes);
            }
            
            // Parse simple string fields
            themeIndex.setThemeProvider(getStringValue(data, "theme_provider"));
            themeIndex.setForProgram(getStringValue(data, "for_program"));
            themeIndex.setDesc(getStringValue(data, "desc"));
            themeIndex.setThemeFormat(getStringValue(data, "theme_format"));
            themeIndex.setDateCreated(getStringValue(data, "date_created"));
            themeIndex.setThemeProviderType(getStringValue(data, "theme_provider_type"));
            themeIndex.setTags(getStringValue(data, "tags"));
            
            // Parse boolean field
            Object certifiedObj = data.get("certified_by_ivan");
            if (certifiedObj instanceof Boolean) {
                themeIndex.setCertifiedByIvan((Boolean) certifiedObj);
            } else if (certifiedObj instanceof String) {
                themeIndex.setCertifiedByIvan("true".equalsIgnoreCase((String) certifiedObj));
            }
            
            logger.info("Successfully parsed index.yml: {}", themeIndex);
            return themeIndex;
            
        } catch (YAMLException e) {
            logger.error("Failed to parse YAML content", e);
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error parsing index.yml", e);
            return null;
        }
    }
    
    /**
     * Parse the present_themes section which has a complex structure
     */
    private Map<String, ThemeIndex.ThemeEntry> parseThemeEntries(Map<String, Object> themesMap) {
        Map<String, ThemeIndex.ThemeEntry> result = new HashMap<>();
        
        for (Map.Entry<String, Object> entry : themesMap.entrySet()) {
            String themeId = entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof String) {
                // Simple case: theme_id: path/to/theme.yml
                result.put(themeId, new ThemeIndex.ThemeEntry((String) value, null));
            } else if (value instanceof Map) {
                // Complex case with images_dir
                @SuppressWarnings("unchecked")
                Map<String, Object> themeData = (Map<String, Object>) value;
                
                String themePath = null;
                String imagesDir = null;
                
                // Look for theme path and images directory
                for (Map.Entry<String, Object> themeEntry : themeData.entrySet()) {
                    String key = themeEntry.getKey();
                    Object val = themeEntry.getValue();
                    
                    if ("images_dir".equals(key) && val instanceof String) {
                        imagesDir = (String) val;
                    } else if (val instanceof String) {
                        // Assume the first string value is the theme path
                        if (themePath == null) {
                            themePath = (String) val;
                        }
                    }
                }
                
                result.put(themeId, new ThemeIndex.ThemeEntry(themePath, imagesDir));
            }
        }
        
        // Handle the alternative format where theme path is directly under the UUID
        if (result.isEmpty()) {
            for (Map.Entry<String, Object> entry : themesMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                // Check if this might be the theme path directly
                if (value instanceof String && key.length() == 36 && key.contains("-")) {
                    // Looks like a UUID, treat the value as theme path
                    result.put(key, new ThemeIndex.ThemeEntry((String) value, null));
                }
            }
        }
        
        logger.debug("Parsed {} theme entries", result.size());
        return result;
    }
    
    /**
     * Safely get a string value from the parsed YAML data
     */
    private String getStringValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : null;
    }
    
    /**
     * Parse index.yml from a string
     * 
     * @param yamlContent The YAML content as a string
     * @return Parsed ThemeIndex or null if parsing fails
     */
    public ThemeIndex parseIndexFromString(String yamlContent) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(yamlContent.getBytes("UTF-8"))) {
            return parseIndex(bis);
        } catch (IOException e) {
            logger.error("Failed to parse YAML from string", e);
            return null;
        }
    }
    
    /**
     * Check if a directory contains a valid index.yml file
     */
    public boolean hasValidIndex(Path themeProviderDirectory) {
        Path indexPath = themeProviderDirectory.resolve(INDEX_FILE_NAME);
        
        if (!Files.exists(indexPath)) {
            return false;
        }
        
        ThemeIndex index = parseIndex(themeProviderDirectory);
        return index != null && index.getThemeProvider() != null;
    }
}