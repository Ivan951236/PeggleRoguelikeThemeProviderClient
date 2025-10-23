package com.ivan.themeprovider;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.DumperOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Manages application configuration stored in YAML format
 */
public class ConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private static final String CONFIG_FILE_NAME = "theme_provider_config.yml";
    
    private final Path configPath;
    private Map<String, Object> config;
    private final Yaml yaml;
    
    public ConfigManager() {
        // Store config in user's home directory
        this.configPath = Paths.get(System.getProperty("user.home"), ".theme_provider_client", CONFIG_FILE_NAME);
        
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        this.yaml = new Yaml(options);
        
        loadConfig();
    }
    
    /**
     * Load configuration from file, create default if not exists
     */
    private void loadConfig() {
        try {
            // Ensure config directory exists
            Files.createDirectories(configPath.getParent());
            
            if (Files.exists(configPath)) {
                try (FileInputStream fis = new FileInputStream(configPath.toFile())) {
                    config = yaml.load(fis);
                    if (config == null) {
                        config = new HashMap<>();
                    }
                }
            } else {
                // Create default configuration
                config = new HashMap<>();
                createDefaultConfig();
                saveConfig();
            }
            
            logger.info("Configuration loaded from: {}", configPath);
        } catch (IOException e) {
            logger.error("Failed to load configuration", e);
            config = new HashMap<>();
            createDefaultConfig();
        }
        
        // Ensure config is never null
        if (config == null) {
            config = new HashMap<>();
            createDefaultConfig();
        }
    }
    
    /**
     * Create default configuration with official theme provider
     */
    private void createDefaultConfig() {
        config.put("selected_program_dir", "");
        
        Map<String, String> installedProviders = new HashMap<>();
        installedProviders.put("official_ivan", "Ivan951236/PeggleRoguelikeThemes");
        config.put("installed_theme_providers", installedProviders);
        
        // UI preferences
        config.put("dark_mode", true);
        config.put("auto_update_themes", true);
        
        logger.info("Created default configuration");
    }
    
    /**
     * Save configuration to file
     */
    public void saveConfig() {
        try {
            Files.createDirectories(configPath.getParent());
            try (FileWriter writer = new FileWriter(configPath.toFile())) {
                yaml.dump(config, writer);
            }
            logger.info("Configuration saved to: {}", configPath);
        } catch (IOException e) {
            logger.error("Failed to save configuration", e);
        }
    }
    
    /**
     * Get the selected program directory
     */
    public String getSelectedProgramDir() {
        return (String) config.getOrDefault("selected_program_dir", "");
    }
    
    /**
     * Set the selected program directory
     */
    public void setSelectedProgramDir(String dir) {
        config.put("selected_program_dir", dir);
        saveConfig();
    }
    
    /**
     * Get installed theme providers
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getInstalledThemeProviders() {
        Object providers = config.get("installed_theme_providers");
        if (providers instanceof Map) {
            return (Map<String, String>) providers;
        }
        return new HashMap<>();
    }
    
    /**
     * Add a theme provider
     */
    public void addThemeProvider(String id, String repository) {
        Map<String, String> providers = getInstalledThemeProviders();
        providers.put(id, repository);
        config.put("installed_theme_providers", providers);
        saveConfig();
    }
    
    /**
     * Remove a theme provider
     */
    public void removeThemeProvider(String id) {
        Map<String, String> providers = getInstalledThemeProviders();
        providers.remove(id);
        config.put("installed_theme_providers", providers);
        saveConfig();
    }
    
    /**
     * Check if dark mode is enabled
     */
    public boolean isDarkMode() {
        return (Boolean) config.getOrDefault("dark_mode", true);
    }
    
    /**
     * Set dark mode preference
     */
    public void setDarkMode(boolean darkMode) {
        config.put("dark_mode", darkMode);
        saveConfig();
    }
    
    /**
     * Check if auto-update is enabled
     */
    public boolean isAutoUpdateEnabled() {
        return (Boolean) config.getOrDefault("auto_update_themes", true);
    }
    
    /**
     * Set auto-update preference
     */
    public void setAutoUpdate(boolean autoUpdate) {
        config.put("auto_update_themes", autoUpdate);
        saveConfig();
    }
    
    /**
     * Get configuration file path
     */
    public Path getConfigPath() {
        return configPath;
    }
}