package com.ivan.themeprovider.ui;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import com.ivan.themeprovider.*;
import com.ivan.themeprovider.ThemeInstaller.ProviderInfo;
import com.ivan.themeprovider.ThemeInstaller.ThemeInfo;
import com.ivan.themeprovider.model.ThemeIndex;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main application window with Material Design 3 interface and navigation
 */
public class MainWindow {
    private static final Logger logger = LoggerFactory.getLogger(MainWindow.class);
    
    private final ConfigManager configManager;
    private final ThemeInstaller themeInstaller;
    private final Stage primaryStage;
    
    // UI Components / Navigation
    private ToggleButton darkModeToggle;
    private BorderPane root;
    private BorderPane contentPane;
    private Label headerTitle;
    private Button headerBackButton;
    private Button headerInstallButton;

    // Markdown rendering
    private final MarkdownRenderer markdownRenderer = new MarkdownRenderer();

    // State
    private ProviderInfo currentProvider;
    private String currentThemeId;

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
        // Root layout with header and content
        root = new BorderPane();
        root.setPadding(new Insets(12));
        HBox headerBox = createHeader();
        contentPane = new BorderPane();
        contentPane.setPadding(new Insets(12));
        root.setTop(headerBox);
        root.setCenter(contentPane);

        Scene scene = new Scene(root, 1100, 750);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Peggle Roguelike Theme Provider Client");
    }
    
    private HBox createHeader() {
        HBox headerBox = new HBox(12);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        headerBackButton = new Button();
        headerBackButton.setGraphic(new FontIcon(MaterialDesignA.ARROW_LEFT));
        headerBackButton.setOnAction(e -> navigateBack());
        headerBackButton.setVisible(false);

        FontIcon appIcon = new FontIcon(MaterialDesignG.GAMEPAD_VARIANT);
        appIcon.setIconSize(28);

        headerTitle = new Label("Theme Provider Client");
        headerTitle.getStyleClass().addAll("title-2");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerInstallButton = new Button("Install");
        headerInstallButton.setGraphic(new FontIcon(MaterialDesignD.DOWNLOAD));
        headerInstallButton.setVisible(false);
        headerInstallButton.setOnAction(e -> installCurrentTheme());

        darkModeToggle = new ToggleButton();
        darkModeToggle.setSelected(configManager.isDarkMode());
        updateDarkModeToggleIcon();
        darkModeToggle.setOnAction(e -> toggleDarkMode());
        darkModeToggle.setTooltip(new Tooltip("Toggle Dark/Light Mode"));

        headerBox.getChildren().addAll(headerBackButton, appIcon, headerTitle, spacer, headerInstallButton, darkModeToggle);
        return headerBox;
    }
    
    // Navigation and views
    private void showProviderSelection() {
        headerBackButton.setVisible(false);
        headerInstallButton.setVisible(false);
        headerTitle.setText("Theme Providers");

        VBox container = new VBox(12);
        container.setPadding(new Insets(8));

        List<ProviderInfo> providers = themeInstaller.getAvailableProviders();
        if (providers.isEmpty()) {
            Label empty = new Label("No providers found. Click Refresh or Install All on the previous screen.");
            container.getChildren().add(empty);
            contentPane.setCenter(container);
            return;
        }

        ListView<ProviderInfo> listView = new ListView<>();
        listView.getItems().setAll(providers);
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(ProviderInfo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                HBox row = new HBox(10);
                Node iconNode = createIconNode(item.getIconPath());
                Label name = new Label(item.getDisplayName());
                name.getStyleClass().add("title-4");
                row.getChildren().addAll(iconNode, name);
                setGraphic(row);
                setText(null);
            }
        });
        listView.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && listView.getSelectionModel().getSelectedItem() != null) {
                showProviderHome(listView.getSelectionModel().getSelectedItem());
            }
        });

        // Bottom bar with actions (Add, Update, Settings at bottom-right)
        HBox bottomBar = new HBox(8);
        Button addBtn = new Button("Add Provider");
        addBtn.setGraphic(new FontIcon(MaterialDesignS.SOURCE_REPOSITORY));
        addBtn.setOnAction(e -> showAddProviderDialog());
        Button updateBtn = new Button("Update Providers");
        updateBtn.setGraphic(new FontIcon(MaterialDesignS.SYNC));
        updateBtn.setOnAction(e -> updateProviders());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button settingsBtn = new Button("Settings");
        settingsBtn.setGraphic(new FontIcon(MaterialDesignC.COG));
        settingsBtn.setOnAction(e -> showSettings());
        bottomBar.getChildren().addAll(addBtn, updateBtn, spacer, settingsBtn);

        container.getChildren().addAll(listView, bottomBar);
        VBox.setVgrow(listView, Priority.ALWAYS);
        contentPane.setCenter(container);
    }

    private void showProviderHome(ProviderInfo provider) {
        this.currentProvider = provider;
        headerBackButton.setVisible(true);
        headerInstallButton.setVisible(false);
        headerTitle.setText(provider.getDisplayName());

        VBox container = new VBox(12);
        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();
        String html = "<p>No homepage.</p>";
        Path mdPath = provider.getHomepagePath();
        if (mdPath != null && Files.exists(mdPath)) {
            String body = markdownRenderer.renderFile(mdPath);
            if (body == null) body = "";
            html = markdownRenderer.createHtmlDocument(body, provider.getDisplayName(), configManager.isDarkMode(), mdPath.getParent());
        }
        engine.loadContent(html);

        Button allThemesBtn = new Button("All Themes");
        allThemesBtn.setGraphic(new FontIcon(MaterialDesignV.VIEW_LIST));
        allThemesBtn.setOnAction(e -> showCategorySelection(provider));

        container.getChildren().addAll(webView, allThemesBtn);
        VBox.setVgrow(webView, Priority.ALWAYS);
        contentPane.setCenter(container);
    }

    private void showCategorySelection(ProviderInfo provider) {
        headerBackButton.setVisible(true);
        headerInstallButton.setVisible(false);
        headerTitle.setText(provider.getDisplayName() + " · Categories");

        VBox box = new VBox(12);
        Label l = new Label("Select a category");
        HBox buttons = new HBox(8);
        Button light = new Button("Light");
        Button dark = new Button("Dark");
        Button other = new Button("Other");
        light.setOnAction(e -> showThemeCatalogForCategory(provider, "light"));
        dark.setOnAction(e -> showThemeCatalogForCategory(provider, "dark"));
        other.setOnAction(e -> showThemeCatalogForCategory(provider, "other"));
        buttons.getChildren().addAll(light, dark, other);
        box.getChildren().addAll(l, buttons);
        contentPane.setCenter(box);
    }

    private void showThemeCatalogForCategory(ProviderInfo provider, String category) {
        headerBackButton.setVisible(true);
        headerInstallButton.setVisible(false);
        headerTitle.setText(provider.getDisplayName() + " · " + category.substring(0,1).toUpperCase() + category.substring(1));

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        VBox content = new VBox(16);
        content.setPadding(new Insets(8));

        ThemeIndex index = provider.getIndex();
        List<Map.Entry<String, ThemeIndex.ThemeEntry>> entries = new ArrayList<>(index.getPresentThemes().entrySet());
        List<Map.Entry<String, ThemeIndex.ThemeEntry>> filtered = entries.stream().filter(e -> {
            String c = Optional.ofNullable(e.getValue().getCategory()).orElse("");
            if ("other".equalsIgnoreCase(category)) {
                return !"dark".equalsIgnoreCase(c) && !"light".equalsIgnoreCase(c);
            }
            return category.equalsIgnoreCase(c);
        }).collect(Collectors.toList());

        if (!filtered.isEmpty()) content.getChildren().add(categorySection(category.substring(0,1).toUpperCase()+category.substring(1)+" Themes", provider, filtered));
        scroll.setContent(content);
        contentPane.setCenter(scroll);
    }

    private VBox categorySection(String title, ProviderInfo provider, List<Map.Entry<String, ThemeIndex.ThemeEntry>> list) {
        VBox box = new VBox(8);
        Label label = new Label(title);
        label.getStyleClass().add("title-3");
        ListView<Map.Entry<String, ThemeIndex.ThemeEntry>> lv = new ListView<>();
        lv.getItems().setAll(list);
        lv.setCellFactory(l -> new ListCell<>() {
            @Override protected void updateItem(Map.Entry<String, ThemeIndex.ThemeEntry> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                String display = Optional.ofNullable(item.getValue().getName()).orElse(item.getKey());
                setText(display + (item.getValue().getThemeTags().isEmpty() ? "" : "  [" + String.join(", ", item.getValue().getThemeTags()) + "]"));
            }
        });
        lv.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && lv.getSelectionModel().getSelectedItem() != null) {
                Map.Entry<String, ThemeIndex.ThemeEntry> sel = lv.getSelectionModel().getSelectedItem();
                showThemeDetail(provider, sel.getKey(), sel.getValue());
            }
        });
        box.getChildren().addAll(label, lv);
        return box;
    }

    private void showThemeDetail(ProviderInfo provider, String themeId, ThemeIndex.ThemeEntry entry) {
        this.currentProvider = provider;
        this.currentThemeId = themeId;
        headerBackButton.setVisible(true);
        headerInstallButton.setVisible(true);
        headerTitle.setText(Optional.ofNullable(entry.getName()).orElse(themeId));

        VBox container = new VBox(12);
        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();
        String html = "<p>No description.</p>";
        if (entry.getMarkdownPath() != null) {
            Path md = provider.getProviderDir().resolve(entry.getMarkdownPath());
            if (Files.exists(md)) {
                String body = markdownRenderer.renderFile(md);
                if (body == null) body = "";
                html = markdownRenderer.createHtmlDocument(body, headerTitle.getText(), configManager.isDarkMode(), md.getParent());
            }
        }
        engine.loadContent(html);
        container.getChildren().add(webView);
        VBox.setVgrow(webView, Priority.ALWAYS);
        contentPane.setCenter(container);
    }

    private void navigateBack() {
        if (currentThemeId != null) {
            // From theme detail back to category selection
            currentThemeId = null;
            showCategorySelection(currentProvider);
            return;
        }
        if (currentProvider != null) {
            // From catalog/home to provider selection
            currentProvider = null;
            showProviderSelection();
            return;
        }
        // Already at main, do nothing
    }

    private Node createIconNode(Path iconPath) {
        double size = 32;
        if (iconPath == null || !Files.exists(iconPath)) {
            FontIcon fallback = new FontIcon(MaterialDesignI.IMAGE);
            fallback.setIconSize((int) size);
            return fallback;
        }
        String name = iconPath.getFileName().toString().toLowerCase();
        if (name.endsWith(".webp")) {
            WebView wv = new WebView();
            wv.setPrefSize(size, size);
            String url = iconPath.toUri().toString();
            wv.getEngine().loadContent("<html><body style='margin:0;padding:0;background:transparent;'><img src='" + url + "' style='height:"+size+"px;width:"+size+"px;object-fit:contain;'/></body></html>");
            return wv;
        } else {
            try {
                Image img = new Image(iconPath.toUri().toString(), size, size, true, true);
                ImageView iv = new ImageView(img);
                iv.setFitWidth(size);
                iv.setFitHeight(size);
                return iv;
            } catch (Exception e) {
                FontIcon fallback = new FontIcon(MaterialDesignI.IMAGE);
                fallback.setIconSize((int) size);
                return fallback;
            }
        }
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
        // Apply new theme and update base colors
        if (darkMode) {
            javafx.application.Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
            root.setStyle("-fx-background-color: #0d1117; -fx-text-fill: #e6edf3;");
        } else {
            javafx.application.Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
            root.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #24292f;");
        }
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
                showProviderSelection();
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
                showProviderSelection();
            } else {
                showErrorDialog("Invalid Repository", "Repository must be in format 'username/reponame'");
            }
        });
    }
    
    private void refreshThemesList() { /* obsolete in new UI */ }
    
    private void installThemes() { /* obsolete in new UI */ }
    
    private void loadConfiguration() {
        String programDir = configManager.getSelectedProgramDir();
        if (programDir.isEmpty()) {
            // Prompt to select directory first
            contentPane.setCenter(createProgramDirPrompt());
        } else {
            showProviderSelection();
        }
    }
    
    private Pane createProgramDirPrompt() {
        VBox box = new VBox(12);
        Label l = new Label("Select the directory containing peggle-roguelike-generator.jar");
        Button b = new Button("Select Directory");
        b.setGraphic(new FontIcon(MaterialDesignF.FOLDER_OPEN));
        b.setOnAction(e -> selectProgramDirectory());
        box.getChildren().addAll(l, b);
        return box;
    }
    
    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void installCurrentTheme() {
        if (currentProvider == null || currentThemeId == null) return;
        headerInstallButton.setDisable(true);
        Task<Boolean> task = new Task<>() {
            @Override protected Boolean call() {
                return themeInstaller.installTheme(currentProvider.getId(), currentThemeId);
            }
            @Override protected void succeeded() {
                headerInstallButton.setDisable(false);
                Boolean ok = getValue();
                Alert a = new Alert(ok ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR, ok ? "Installed successfully" : "Installation failed");
                a.setHeaderText(null);
                a.showAndWait();
            }
            @Override protected void failed() {
                headerInstallButton.setDisable(false);
                Alert a = new Alert(Alert.AlertType.ERROR, "Installation error: " + getException().getMessage());
                a.setHeaderText(null);
                a.showAndWait();
            }
        };
        new Thread(task).start();
    }

    public void shutdown() {
        themeInstaller.shutdown();
    }

    private void updateProviders() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText("Updating providers...");
        a.setContentText("This may take a moment.");
        a.show();
        Task<Boolean> task = new Task<>() {
            @Override protected Boolean call() {
                return themeInstaller.updateAllProviders(null);
            }
            @Override protected void succeeded() {
                a.close();
                boolean ok = getValue();
                Alert done = new Alert(ok ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR, ok ? "Providers updated." : "Some providers failed to update.");
                done.setHeaderText(null);
                done.showAndWait();
                showProviderSelection();
            }
            @Override protected void failed() {
                a.close();
                Alert err = new Alert(Alert.AlertType.ERROR, "Update failed: " + getException().getMessage());
                err.setHeaderText(null);
                err.showAndWait();
            }
        };
        new Thread(task).start();
    }

    private void showSettings() {
        headerBackButton.setVisible(true);
        headerInstallButton.setVisible(false);
        headerTitle.setText("Settings");
        VBox box = new VBox(12);
        box.setPadding(new Insets(8));

        // Program directory
        Label progLbl = new Label("Program Directory");
        TextField progField = new TextField(configManager.getSelectedProgramDir());
        progField.setEditable(false);
        Button progSelect = new Button("Select");
        progSelect.setOnAction(e -> selectProgramDirectory());
        HBox progRow = new HBox(8, progLbl, progField, progSelect);
        HBox.setHgrow(progField, Priority.ALWAYS);

        // Providers root
        Label provLbl = new Label("Providers Directory");
        TextField provField = new TextField(configManager.getProvidersRoot().toString());
        provField.setEditable(false);
        Button provSelect = new Button("Change");
        provSelect.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select Providers Directory");
            File current = configManager.getProvidersRoot().toFile();
            if (current.exists()) chooser.setInitialDirectory(current);
            File sel = chooser.showDialog(primaryStage);
            if (sel != null) {
                Path newRoot = sel.toPath();
                // Check write permission
                try {
                    Files.createDirectories(newRoot);
                    if (!Files.isWritable(newRoot)) {
                        showErrorDialog("Permission Error", "Selected directory is not writable.");
                        return;
                    }
                } catch (Exception ex) {
                    showErrorDialog("Error", "Failed to access selected directory: " + ex.getMessage());
                    return;
                }
                boolean moved = configManager.moveProvidersRoot(newRoot);
                if (moved) {
                    provField.setText(newRoot.toString());
                } else {
                    showErrorDialog("Move Failed", "Could not move providers to the new directory.");
                }
            }
        });
        HBox provRow = new HBox(8, provLbl, provField, provSelect);
        HBox.setHgrow(provField, Priority.ALWAYS);

        box.getChildren().addAll(progRow, provRow);
        contentPane.setCenter(box);
    }
}