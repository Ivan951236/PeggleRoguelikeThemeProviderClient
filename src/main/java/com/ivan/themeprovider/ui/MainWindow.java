package com.ivan.themeprovider.ui;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import com.ivan.themeprovider.*;
import com.ivan.themeprovider.ThemeInstaller.ThemeInfo;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignF;
import org.kordamp.ikonli.materialdesign2.MaterialDesignS;
import org.kordamp.ikonli.materialdesign2.MaterialDesignG;
import org.kordamp.ikonli.materialdesign2.MaterialDesignD;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * Main application window with Material Design 3 interface
 */
public class MainWindow {
    private static final Logger logger = LoggerFactory.getLogger(MainWindow.class);
    
    private final ConfigManager configManager;
    private final ThemeInstaller themeInstaller;
    private final Stage primaryStage;
    
    // UI Components
    private Label statusLabel;
    private TextField programDirField;
    private Button selectDirButton;
    private Button installThemesButton;
    private ProgressBar progressBar;
    private TableView<ThemeInfo> themesTable;
    private ToggleButton darkModeToggle;
    private TextArea logArea;
    
    public MainWindow(Stage primaryStage, ConfigManager configManager) {
        this.primaryStage = primaryStage;
        this.configManager = configManager;
        this.themeInstaller = new ThemeInstaller(configManager);
        
        setupTheme();
        initializeUI();
        loadConfiguration();
    }
    
    private void setupTheme() {
        // Apply Material Design 3 theme based on user preference
        if (configManager.isDarkMode()) {
            javafx.application.Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        } else {
            javafx.application.Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        }
    }
    
    private void initializeUI() {
        // Main layout
        VBox mainLayout = new VBox(16);
        mainLayout.setPadding(new Insets(20));
        
        // Header
        HBox headerBox = createHeader();
        
        // Program directory selection
        VBox directionSection = createDirectorySection();
        
        // Theme providers section
        VBox providersSection = createProvidersSection();
        
        // Available themes table
        VBox themesSection = createThemesSection();
        
        // Progress and status
        VBox statusSection = createStatusSection();
        
        // Log area
        VBox logSection = createLogSection();
        
        mainLayout.getChildren().addAll(
            headerBox, directionSection, providersSection, 
            themesSection, statusSection, logSection
        );
        
        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        Scene scene = new Scene(scrollPane, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Peggle Roguelike Theme Provider Client");
        
        // Set application icon
        // Note: JavaFX doesn't support FontIcon as window icon directly,
        // but we'll use it in the header
    }
    
    private HBox createHeader() {
        HBox headerBox = new HBox(16);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        FontIcon appIcon = new FontIcon(MaterialDesignG.GAMEPAD_VARIANT);
        appIcon.setIconSize(32);
        appIcon.getStyleClass().add("accent");
        
        Label titleLabel = new Label("Theme Provider Client");
        titleLabel.getStyleClass().addAll("title-1");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Dark mode toggle
        darkModeToggle = new ToggleButton();
        darkModeToggle.setSelected(configManager.isDarkMode());
        updateDarkModeToggleIcon();
        darkModeToggle.setOnAction(e -> toggleDarkMode());
        darkModeToggle.setTooltip(new Tooltip("Toggle Dark/Light Mode"));
        
        headerBox.getChildren().addAll(appIcon, titleLabel, spacer, darkModeToggle);
        
        return headerBox;
    }
    
    private VBox createDirectorySection() {
        VBox section = new VBox(8);
        
        Label sectionLabel = new Label("Program Directory");
        sectionLabel.getStyleClass().add("title-3");
        
        HBox dirBox = new HBox(8);
        dirBox.setAlignment(Pos.CENTER_LEFT);
        
        programDirField = new TextField();
        programDirField.setPromptText("Select the directory containing peggle-roguelike-generator.jar");
        programDirField.setEditable(false);
        HBox.setHgrow(programDirField, Priority.ALWAYS);
        
        selectDirButton = new Button("Select Directory");
        selectDirButton.setGraphic(new FontIcon(MaterialDesignF.FOLDER_OPEN));
        selectDirButton.setOnAction(e -> selectProgramDirectory());
        
        dirBox.getChildren().addAll(programDirField, selectDirButton);
        
        section.getChildren().addAll(sectionLabel, dirBox);
        return section;
    }
    
    private VBox createProvidersSection() {
        VBox section = new VBox(8);
        
        Label sectionLabel = new Label("Theme Providers");
        sectionLabel.getStyleClass().add("title-3");
        
        HBox providersBox = new HBox(8);
        providersBox.setAlignment(Pos.CENTER_LEFT);
        
        Button addProviderButton = new Button("Add Provider");
        addProviderButton.setGraphic(new FontIcon(MaterialDesignS.SOURCE_REPOSITORY));
        addProviderButton.setOnAction(e -> showAddProviderDialog());
        
        Button refreshProvidersButton = new Button("Refresh");
        refreshProvidersButton.setGraphic(new FontIcon(MaterialDesignS.SYNC));
        refreshProvidersButton.setOnAction(e -> refreshThemesList());
        
        installThemesButton = new Button("Install All Themes");
        installThemesButton.setGraphic(new FontIcon(MaterialDesignD.DOWNLOAD));
        installThemesButton.setOnAction(e -> installThemes());
        
        providersBox.getChildren().addAll(addProviderButton, refreshProvidersButton, installThemesButton);
        
        section.getChildren().addAll(sectionLabel, providersBox);
        return section;
    }
    
    private VBox createThemesSection() {
        VBox section = new VBox(8);
        
        Label sectionLabel = new Label("Available Themes");
        sectionLabel.getStyleClass().add("title-3");
        
        // Create themes table
        themesTable = new TableView<>();
        
        TableColumn<ThemeInfo, String> nameCol = new TableColumn<>("Theme Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);
        
        TableColumn<ThemeInfo, String> providerCol = new TableColumn<>("Provider");
        providerCol.setCellValueFactory(new PropertyValueFactory<>("providerId"));
        providerCol.setPrefWidth(120);
        
        TableColumn<ThemeInfo, Boolean> certifiedCol = new TableColumn<>("Certified");
        certifiedCol.setCellValueFactory(new PropertyValueFactory<>("certified"));
        certifiedCol.setPrefWidth(80);
        certifiedCol.setCellFactory(col -> new TableCell<ThemeInfo, Boolean>() {
            @Override
            protected void updateItem(Boolean certified, boolean empty) {
                super.updateItem(certified, empty);
                if (empty || certified == null) {
                    setGraphic(null);
                } else if (certified) {
                    FontIcon icon = new FontIcon(MaterialDesignC.CHECK_CIRCLE);
                    icon.getStyleClass().add("accent");
                    setGraphic(icon);
                } else {
                    setGraphic(null);
                }
                setText(null);
            }
        });
        
        TableColumn<ThemeInfo, Boolean> officialCol = new TableColumn<>("Official");
        officialCol.setCellValueFactory(new PropertyValueFactory<>("official"));
        officialCol.setPrefWidth(80);
        officialCol.setCellFactory(col -> new TableCell<ThemeInfo, Boolean>() {
            @Override
            protected void updateItem(Boolean official, boolean empty) {
                super.updateItem(official, empty);
                if (empty || official == null) {
                    setGraphic(null);
                } else if (official) {
                    FontIcon icon = new FontIcon(MaterialDesignS.STAR);
                    icon.getStyleClass().add("success");
                    setGraphic(icon);
                } else {
                    setGraphic(null);
                }
                setText(null);
            }
        });
        
        TableColumn<ThemeInfo, String> tagsCol = new TableColumn<>("Tags");
        tagsCol.setCellValueFactory(cellData -> {
            List<String> tags = cellData.getValue().getTags();
            return new javafx.beans.property.SimpleStringProperty(String.join(", ", tags));
        });
        tagsCol.setPrefWidth(300);
        
        themesTable.getColumns().addAll(nameCol, providerCol, certifiedCol, officialCol, tagsCol);
        themesTable.setPrefHeight(200);
        
        section.getChildren().addAll(sectionLabel, themesTable);
        return section;
    }
    
    private VBox createStatusSection() {
        VBox section = new VBox(8);
        
        statusLabel = new Label("Ready");
        statusLabel.getStyleClass().add("title-4");
        
        progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setVisible(false);
        
        section.getChildren().addAll(statusLabel, progressBar);
        return section;
    }
    
    private VBox createLogSection() {
        VBox section = new VBox(8);
        
        Label sectionLabel = new Label("Log");
        sectionLabel.getStyleClass().add("title-3");
        
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefRowCount(6);
        logArea.getStyleClass().add("text-small");
        
        section.getChildren().addAll(sectionLabel, logArea);
        return section;
    }
    
    private void updateDarkModeToggleIcon() {
        if (darkModeToggle.isSelected()) {
            darkModeToggle.setGraphic(new FontIcon(MaterialDesignW.WEATHER_NIGHT));
        } else {
            darkModeToggle.setGraphic(new FontIcon(MaterialDesignW.WHITE_BALANCE_SUNNY));
        }
    }
    
    private void toggleDarkMode() {
        boolean darkMode = darkModeToggle.isSelected();
        configManager.setDarkMode(darkMode);
        updateDarkModeToggleIcon();
        
        // Apply new theme
        if (darkMode) {
            javafx.application.Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        } else {
            javafx.application.Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        }
        
        appendLog("Switched to " + (darkMode ? "dark" : "light") + " mode");
    }
    
    private void selectProgramDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Program Directory");
        
        // Start from the current selection if available
        String currentDir = configManager.getSelectedProgramDir();
        if (!currentDir.isEmpty()) {
            File currentFile = new File(currentDir);
            if (currentFile.exists() && currentFile.isDirectory()) {
                chooser.setInitialDirectory(currentFile);
            }
        }
        
        File selectedDir = chooser.showDialog(primaryStage);
        if (selectedDir != null) {
            Path selectedPath = selectedDir.toPath();
            ProgramValidator.ValidationResult validation = ProgramValidator.validateProgramDirectory(selectedPath);
            
            if (validation.isValid()) {
                configManager.setSelectedProgramDir(selectedDir.getAbsolutePath());
                programDirField.setText(selectedDir.getAbsolutePath());
                updateStatus("Program directory selected: " + selectedDir.getName());
                appendLog("Selected program directory: " + selectedDir.getAbsolutePath());
                refreshThemesList();
            } else {
                showErrorDialog("Invalid Directory", validation.getMessage());
            }
        }
    }
    
    private void showAddProviderDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Theme Provider");
        dialog.setHeaderText("Add GitHub Repository");
        dialog.setContentText("Repository (username/reponame):");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(repository -> {
            if (repository.contains("/")) {
                String providerId = "provider_" + System.currentTimeMillis();
                configManager.addThemeProvider(providerId, repository);
                appendLog("Added theme provider: " + repository);
                refreshThemesList();
            } else {
                showErrorDialog("Invalid Repository", "Repository must be in format 'username/reponame'");
            }
        });
    }
    
    private void refreshThemesList() {
        Task<List<ThemeInfo>> task = new Task<List<ThemeInfo>>() {
            @Override
            protected List<ThemeInfo> call() {
                return themeInstaller.getAvailableThemes();
            }
            
            @Override
            protected void succeeded() {
                List<ThemeInfo> themes = getValue();
                themesTable.getItems().setAll(themes);
                updateStatus("Found " + themes.size() + " available themes");
                appendLog("Refreshed themes list: " + themes.size() + " themes found");
            }
            
            @Override
            protected void failed() {
                Throwable exception = getException();
                logger.error("Failed to refresh themes list", exception);
                updateStatus("Failed to refresh themes list");
                appendLog("Error refreshing themes: " + exception.getMessage());
            }
        };
        
        new Thread(task).start();
    }
    
    private void installThemes() {
        if (configManager.getSelectedProgramDir().isEmpty()) {
            showErrorDialog("No Directory Selected", "Please select a program directory first.");
            return;
        }
        
        installThemesButton.setDisable(true);
        progressBar.setVisible(true);
        progressBar.setProgress(0);
        
        Task<Void> installTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                themeInstaller.installAllThemes(new ThemeInstaller.InstallationProgressCallback() {
                    @Override
                    public void onProgressUpdate(String message, double progress) {
                        Platform.runLater(() -> {
                            updateStatus(message);
                            progressBar.setProgress(progress);
                        });
                    }
                    
                    @Override
                    public void onThemeInstalled(String themeId, String themeName, boolean success) {
                        Platform.runLater(() -> {
                            String status = success ? "✓" : "✗";
                            appendLog(status + " " + themeName + " (" + themeId + ")");
                        });
                    }
                    
                    @Override
                    public void onComplete(int successful, int failed) {
                        Platform.runLater(() -> {
                            String message = String.format("Installation complete: %d successful, %d failed", 
                                                          successful, failed);
                            updateStatus(message);
                            appendLog(message);
                        });
                    }
                }).join(); // Wait for completion
                
                return null;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    installThemesButton.setDisable(false);
                    progressBar.setVisible(false);
                    refreshThemesList();
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    installThemesButton.setDisable(false);
                    progressBar.setVisible(false);
                    Throwable exception = getException();
                    logger.error("Theme installation failed", exception);
                    updateStatus("Installation failed: " + exception.getMessage());
                    appendLog("Installation error: " + exception.getMessage());
                });
            }
        };
        
        new Thread(installTask).start();
    }
    
    private void loadConfiguration() {
        String programDir = configManager.getSelectedProgramDir();
        if (!programDir.isEmpty()) {
            programDirField.setText(programDir);
            refreshThemesList();
        }
        
        updateStatus("Application initialized");
        appendLog("Theme Provider Client started");
        
        // Log configured providers
        configManager.getInstalledThemeProviders().forEach((id, repo) -> 
            appendLog("Configured provider: " + id + " -> " + repo));
    }
    
    private void updateStatus(String message) {
        statusLabel.setText(message);
    }
    
    private void appendLog(String message) {
        Platform.runLater(() -> {
            String timestamp = java.time.LocalTime.now().toString().substring(0, 8);
            logArea.appendText("[" + timestamp + "] " + message + "\n");
        });
    }
    
    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public void shutdown() {
        themeInstaller.shutdown();
    }
}