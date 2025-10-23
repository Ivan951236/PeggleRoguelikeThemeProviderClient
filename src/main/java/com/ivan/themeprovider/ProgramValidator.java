package com.ivan.themeprovider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Validates that a directory contains the required Peggle Roguelike Generator files
 */
public class ProgramValidator {
    private static final Logger logger = LoggerFactory.getLogger(ProgramValidator.class);
    private static final String JAR_PREFIX = "peggle-roguelike-generator";
    private static final String JAR_EXTENSION = ".jar";
    private static final String CUSTOM_THEMES_DIR = "customThemes";
    private static final String THEME_PROVIDERS_DIR = "themeProviders";
    
    /**
     * Validation result containing the status and paths
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        private final Path jarFile;
        private final Path customThemesDir;
        private final Path themeProvidersDir;
        
        public ValidationResult(boolean valid, String message, Path jarFile, 
                              Path customThemesDir, Path themeProvidersDir) {
            this.valid = valid;
            this.message = message;
            this.jarFile = jarFile;
            this.customThemesDir = customThemesDir;
            this.themeProvidersDir = themeProvidersDir;
        }
        
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public Path getJarFile() { return jarFile; }
        public Path getCustomThemesDir() { return customThemesDir; }
        public Path getThemeProvidersDir() { return themeProvidersDir; }
    }
    
    /**
     * Validate that the directory contains the required files and directories
     * 
     * @param directory The directory to validate
     * @return ValidationResult with status and found paths
     */
    public static ValidationResult validateProgramDirectory(Path directory) {
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            return new ValidationResult(false, "Directory does not exist or is not a directory", 
                                      null, null, null);
        }
        
        try {
            // Find JAR file starting with "peggle-roguelike-generator"
            List<Path> jarFiles = Files.list(directory)
                .filter(Files::isRegularFile)
                .filter(path -> {
                    String fileName = path.getFileName().toString().toLowerCase();
                    return fileName.startsWith(JAR_PREFIX.toLowerCase()) && 
                           fileName.endsWith(JAR_EXTENSION);
                })
                .collect(Collectors.toList());
            
            if (jarFiles.isEmpty()) {
                return new ValidationResult(false, 
                    "No JAR file found starting with '" + JAR_PREFIX + "'", 
                    null, null, null);
            }
            
            if (jarFiles.size() > 1) {
                logger.warn("Multiple JAR files found, using the first one: {}", 
                           jarFiles.get(0).getFileName());
            }
            
            Path jarFile = jarFiles.get(0);
            
            // Check for customThemes directory
            Path customThemesDir = directory.resolve(CUSTOM_THEMES_DIR);
            if (!Files.exists(customThemesDir)) {
                // Try to create it
                try {
                    Files.createDirectory(customThemesDir);
                    logger.info("Created missing customThemes directory: {}", customThemesDir);
                } catch (IOException e) {
                    return new ValidationResult(false, 
                        "customThemes directory does not exist and cannot be created", 
                        jarFile, null, null);
                }
            }
            
            if (!Files.isDirectory(customThemesDir)) {
                return new ValidationResult(false, 
                    "customThemes exists but is not a directory", 
                    jarFile, null, null);
            }
            
            // Check/create themeProviders directory
            Path themeProvidersDir = directory.resolve(THEME_PROVIDERS_DIR);
            if (!Files.exists(themeProvidersDir)) {
                try {
                    Files.createDirectory(themeProvidersDir);
                    logger.info("Created themeProviders directory: {}", themeProvidersDir);
                } catch (IOException e) {
                    return new ValidationResult(false, 
                        "themeProviders directory does not exist and cannot be created", 
                        jarFile, customThemesDir, null);
                }
            }
            
            logger.info("Program directory validation successful: {}", directory);
            logger.info("Found JAR: {}", jarFile.getFileName());
            logger.info("CustomThemes directory: {}", customThemesDir);
            logger.info("ThemeProviders directory: {}", themeProvidersDir);
            
            return new ValidationResult(true, "Validation successful", 
                                      jarFile, customThemesDir, themeProvidersDir);
            
        } catch (IOException e) {
            logger.error("Error validating program directory", e);
            return new ValidationResult(false, 
                "Error reading directory: " + e.getMessage(), 
                null, null, null);
        }
    }
    
    /**
     * Get the theme providers directory for a validated program directory
     */
    public static Path getThemeProvidersDirectory(Path programDirectory) {
        return programDirectory.resolve(THEME_PROVIDERS_DIR);
    }
    
    /**
     * Get the custom themes directory for a validated program directory
     */
    public static Path getCustomThemesDirectory(Path programDirectory) {
        return programDirectory.resolve(CUSTOM_THEMES_DIR);
    }
    
    /**
     * Check if a directory looks like it might contain the program
     * (less strict check for suggestions)
     */
    public static boolean mightContainProgram(Path directory) {
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            return false;
        }
        
        try {
            return Files.list(directory)
                .filter(Files::isRegularFile)
                .anyMatch(path -> {
                    String fileName = path.getFileName().toString().toLowerCase();
                    return fileName.contains("peggle") || fileName.contains("roguelike");
                });
        } catch (IOException e) {
            return false;
        }
    }
}