package com.ivan.themeprovider;

import com.ivan.themeprovider.ui.MainWindow;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for the Theme Provider Client application
 */
public class ThemeProviderClient extends Application {
    private static final Logger logger = LoggerFactory.getLogger(ThemeProviderClient.class);
    
    private ConfigManager configManager;
    private MainWindow mainWindow;
    
    @Override
    public void init() throws Exception {
        super.init();
        
        logger.info("Initializing Theme Provider Client...");
        
        // Initialize configuration manager
        configManager = new ConfigManager();
        
        logger.info("Configuration loaded from: {}", configManager.getConfigPath());
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        logger.info("Starting Theme Provider Client UI...");
        
        try {
            // Create and show the main window
            mainWindow = new MainWindow(primaryStage, configManager);
            primaryStage.show();
            
            logger.info("Theme Provider Client started successfully");
            
        } catch (Exception e) {
            logger.error("Failed to start application", e);
            throw e;
        }
    }
    
    @Override
    public void stop() throws Exception {
        logger.info("Shutting down Theme Provider Client...");
        
        try {
            if (mainWindow != null) {
                mainWindow.shutdown();
            }
            
            // Save any final configuration changes
            if (configManager != null) {
                configManager.saveConfig();
            }
            
            logger.info("Theme Provider Client shutdown complete");
            
        } catch (Exception e) {
            logger.error("Error during shutdown", e);
        }
        
        super.stop();
    }
    
    /**
     * Main method to launch the application
     */
    public static void main(String[] args) {
        logger.info("Theme Provider Client starting...");
        logger.info("Java version: {}", System.getProperty("java.version"));
        logger.info("JavaFX version: {}", System.getProperty("javafx.version"));
        logger.info("OS: {} {}", System.getProperty("os.name"), System.getProperty("os.version"));
        
        try {
            // Enable high DPI support if available
            System.setProperty("prism.allowhidpi", "true");
            
            // Launch the JavaFX application
            launch(args);
            
        } catch (Exception e) {
            logger.error("Failed to start Theme Provider Client", e);
            System.exit(1);
        }
    }
    
}