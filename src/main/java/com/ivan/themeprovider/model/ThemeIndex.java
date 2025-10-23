package com.ivan.themeprovider.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Represents the theme provider index.yml configuration
 */
public class ThemeIndex {
    private Map<String, ThemeEntry> presentThemes = new HashMap<>();
    private String themeProvider;
    private String forProgram;
    private String desc;
    private boolean certifiedByIvan;
    private String themeFormat;
    private String dateCreated;
    private String themeProviderType;
    private String tags;
    
    // Getters and setters
    public Map<String, ThemeEntry> getPresentThemes() { return presentThemes; }
    public void setPresentThemes(Map<String, ThemeEntry> presentThemes) { this.presentThemes = presentThemes; }
    
    public String getThemeProvider() { return themeProvider; }
    public void setThemeProvider(String themeProvider) { this.themeProvider = themeProvider; }
    
    public String getForProgram() { return forProgram; }
    public void setForProgram(String forProgram) { this.forProgram = forProgram; }
    
    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }
    
    public boolean isCertifiedByIvan() { return certifiedByIvan; }
    public void setCertifiedByIvan(boolean certifiedByIvan) { this.certifiedByIvan = certifiedByIvan; }
    
    public String getThemeFormat() { return themeFormat; }
    public void setThemeFormat(String themeFormat) { this.themeFormat = themeFormat; }
    
    public String getDateCreated() { return dateCreated; }
    public void setDateCreated(String dateCreated) { this.dateCreated = dateCreated; }
    
    public String getThemeProviderType() { return themeProviderType; }
    public void setThemeProviderType(String themeProviderType) { this.themeProviderType = themeProviderType; }
    
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    
    /**
     * Check if this is an official theme provider
     */
    public boolean isOfficial() {
        return "official".equalsIgnoreCase(themeProviderType);
    }
    
    /**
     * Get parsed tags as a list
     */
    public List<String> getTagsList() {
        if (tags == null || tags.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(tags.split("\\s*,\\s*"));
    }
    
    /**
     * Parse the date created string
     */
    public LocalDate getParsedDateCreated() {
        if (dateCreated == null || dateCreated.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Try common date formats
            DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ISO_LOCAL_DATE
            };
            
            for (DateTimeFormatter formatter : formatters) {
                try {
                    return LocalDate.parse(dateCreated, formatter);
                } catch (DateTimeParseException ignored) {
                    // Try next formatter
                }
            }
        } catch (Exception ignored) {
            // Return null if parsing fails
        }
        
        return null;
    }
    
    /**
     * Check if tags contain NSFW content
     */
    public boolean hasNsfwContent() {
        List<String> tagsList = getTagsList();
        return tagsList.stream()
                .anyMatch(tag -> tag.toLowerCase().contains("nsfw"));
    }
    
    @Override
    public String toString() {
        return "ThemeIndex{" +
                "themeProvider='" + themeProvider + '\'' +
                ", forProgram='" + forProgram + '\'' +
                ", desc='" + desc + '\'' +
                ", certifiedByIvan=" + certifiedByIvan +
                ", themeProviderType='" + themeProviderType + '\'' +
                ", themes=" + presentThemes.size() +
                '}';
    }
    
    /**
     * Represents a single theme entry from the index
     */
    public static class ThemeEntry {
        private String themePath;
        private String imagesDir;
        
        public ThemeEntry() {}
        
        public ThemeEntry(String themePath, String imagesDir) {
            this.themePath = themePath;
            this.imagesDir = imagesDir;
        }
        
        public String getThemePath() { return themePath; }
        public void setThemePath(String themePath) { this.themePath = themePath; }
        
        public String getImagesDir() { return imagesDir; }
        public void setImagesDir(String imagesDir) { this.imagesDir = imagesDir; }
        
        @Override
        public String toString() {
            return "ThemeEntry{" +
                    "themePath='" + themePath + '\'' +
                    ", imagesDir='" + imagesDir + '\'' +
                    '}';
        }
    }
}