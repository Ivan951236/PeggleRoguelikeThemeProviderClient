package com.ivan.themeprovider;

import com.ivan.themeprovider.model.ThemeIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Handles theme installation from theme providers to the program's customThemes directory
 */
public class ThemeInstaller {
    private static final Logger logger = LoggerFactory.getLogger(ThemeInstaller.class);
    
    private final ConfigManager configManager;
    private final GitHubHandler gitHubHandler;
    private final ThemeIndexParser indexParser;
    private final ExecutorService executorService;
    private final MarkdownRenderer markdownRenderer;
    
    /**
     * Progress callback for theme installation operations
     */
    public interface InstallationProgressCallback {
        void onProgressUpdate(String message, double progress);
        void onThemeInstalled(String themeId, String themeName, boolean success);
        void onComplete(int successful, int failed);
    }
    
    /**
     * Theme installation result
     */
    public static class InstallationResult {
        private final boolean success;
        private final String message;
        private final List<String> installedThemes;
        private final List<String> failedThemes;
        
        public InstallationResult(boolean success, String message, 
                                List<String> installedThemes, List<String> failedThemes) {
            this.success = success;
            this.message = message;
            this.installedThemes = installedThemes;
            this.failedThemes = failedThemes;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public List<String> getInstalledThemes() { return installedThemes; }
        public List<String> getFailedThemes() { return failedThemes; }
    }
    
    public ThemeInstaller(ConfigManager configManager) {
        this.configManager = configManager;
        this.gitHubHandler = new GitHubHandler();
        this.indexParser = new ThemeIndexParser();
        this.executorService = Executors.newFixedThreadPool(3);
        this.markdownRenderer = new MarkdownRenderer();
    }
    
    /**
     * Install themes from all configured theme providers
     */
    public CompletableFuture<InstallationResult> installAllThemes(
            InstallationProgressCallback progressCallback) {
        
        return CompletableFuture.supplyAsync(() -> {
            String programDir = configManager.getSelectedProgramDir();
            if (programDir.isEmpty()) {
                return new InstallationResult(false, "No program directory selected", 
                                            Collections.emptyList(), Collections.emptyList());
            }
            
            Path programPath = Paths.get(programDir);
            ProgramValidator.ValidationResult validation = ProgramValidator.validateProgramDirectory(programPath);
            
            if (!validation.isValid()) {
                return new InstallationResult(false, "Invalid program directory: " + validation.getMessage(),
                                            Collections.emptyList(), Collections.emptyList());
            }
            
            Map<String, String> themeProviders = configManager.getInstalledThemeProviders();
            if (themeProviders.isEmpty()) {
                return new InstallationResult(false, "No theme providers configured",
                                            Collections.emptyList(), Collections.emptyList());
            }
            
            List<String> installed = new ArrayList<>();
            List<String> failed = new ArrayList<>();
            
            int providerIndex = 0;
            for (Map.Entry<String, String> provider : themeProviders.entrySet()) {
                double baseProgress = (double) providerIndex / themeProviders.size();
                double providerProgressRange = 1.0 / themeProviders.size();
                
                if (progressCallback != null) {
                    progressCallback.onProgressUpdate(
                        "Processing provider: " + provider.getKey(), baseProgress);
                }
                
                InstallationResult providerResult = installThemesFromProvider(
                    provider.getKey(), provider.getValue(), configManager.getProvidersRoot(),
                    validation.getCustomThemesDir(), new InstallationProgressCallback() {
                        @Override
                        public void onProgressUpdate(String message, double progress) {
                            if (progressCallback != null) {
                                double totalProgress = baseProgress + (progress * providerProgressRange);
                                progressCallback.onProgressUpdate(message, totalProgress);
                            }
                        }
                        
                        @Override
                        public void onThemeInstalled(String themeId, String themeName, boolean success) {
                            // Not used in this context
                        }
                        
                        @Override
                        public void onComplete(int successful, int failed) {
                            // Not used in this context
                        }
                    });
                
                installed.addAll(providerResult.getInstalledThemes());
                failed.addAll(providerResult.getFailedThemes());
                
                providerIndex++;
            }
            
            if (progressCallback != null) {
                progressCallback.onComplete(installed.size(), failed.size());
            }
            
            String message = String.format("Installation complete. %d themes installed, %d failed.",
                                          installed.size(), failed.size());
            
            return new InstallationResult(true, message, installed, failed);
            
        }, executorService);
    }
    
    /**
     * Install themes from a specific theme provider
     */
    public InstallationResult installThemesFromProvider(String providerId, String repositoryName,
                                                       Path providersRootDir, Path customThemesDir,
                                                       InstallationProgressCallback progressCallback) {
        
        try {
            // Clone or update the theme provider repository
            if (progressCallback != null) {
                progressCallback.onProgressUpdate("Cloning/updating " + repositoryName, 0.1);
            }
            
            boolean cloneSuccess = gitHubHandler.cloneRepository(repositoryName, providersRootDir,
                new GitHubHandler.ProgressCallback() {
                    @Override
                    public void onProgress(String task, int completed, int total) {
                        if (progressCallback != null && total > 0) {
                            double progress = 0.1 + (0.2 * completed / total);
                            progressCallback.onProgressUpdate(task, progress);
                        }
                    }
                    
                    @Override
                    public void onMessage(String message) {
                        if (progressCallback != null) {
                            progressCallback.onProgressUpdate(message, 0.2);
                        }
                    }
                });
            
            if (!cloneSuccess) {
                return new InstallationResult(false, "Failed to clone repository: " + repositoryName,
                                            Collections.emptyList(), Collections.emptyList());
            }
            
            // Find the cloned repository directory
            String repoName = repositoryName.substring(repositoryName.lastIndexOf('/') + 1);
            Path providerDir = providersRootDir.resolve(repoName);
            
            if (progressCallback != null) {
                progressCallback.onProgressUpdate("Parsing theme index", 0.3);
            }
            
            // Parse the theme index
            ThemeIndex themeIndex = indexParser.parseIndex(providerDir);
            if (themeIndex == null) {
                return new InstallationResult(false, "Failed to parse index.yml from: " + repositoryName,
                                            Collections.emptyList(), Collections.emptyList());
            }
            
            // Install each theme
            Map<String, ThemeIndex.ThemeEntry> themes = themeIndex.getPresentThemes();
            List<String> installed = new ArrayList<>();
            List<String> failed = new ArrayList<>();
            
            int themeIndex_i = 0;
            for (Map.Entry<String, ThemeIndex.ThemeEntry> themeEntry : themes.entrySet()) {
                double themeProgress = 0.3 + (0.7 * themeIndex_i / themes.size());
                
                String themeId = themeEntry.getKey();
                ThemeIndex.ThemeEntry theme = themeEntry.getValue();
                
                if (progressCallback != null) {
                    progressCallback.onProgressUpdate("Installing theme: " + themeId, themeProgress);
                }
                
                boolean installSuccess = installSingleTheme(themeId, theme, providerDir, customThemesDir);
                
                if (installSuccess) {
                    installed.add(themeId);
                    logger.info("Successfully installed theme: {}", themeId);
                } else {
                    failed.add(themeId);
                    logger.warn("Failed to install theme: {}", themeId);
                }
                
                if (progressCallback != null) {
                    progressCallback.onThemeInstalled(themeId, theme.getThemePath(), installSuccess);
                }
                
                themeIndex_i++;
            }
            
            return new InstallationResult(true, "Provider themes processed", installed, failed);
            
        } catch (Exception e) {
            logger.error("Error installing themes from provider: {}", repositoryName, e);
            return new InstallationResult(false, "Error: " + e.getMessage(),
                                        Collections.emptyList(), Collections.emptyList());
        }
    }
    
    /**
     * Install a single theme to the customThemes directory
     */
    private boolean installSingleTheme(String themeId, ThemeIndex.ThemeEntry theme, 
                                     Path providerDir, Path customThemesDir) {
        if (theme.getThemePath() == null || theme.getThemePath().isEmpty()) {
            logger.warn("Theme {} has no theme path specified", themeId);
            return false;
        }
        
        try {
            // Source theme file
            Path sourceThemePath = providerDir.resolve(theme.getThemePath());
            if (!Files.exists(sourceThemePath)) {
                logger.warn("Theme file does not exist: {}", sourceThemePath);
                return false;
            }
            
            // Destination theme file
            String themeFileName = sourceThemePath.getFileName().toString();
            Path destThemePath = customThemesDir.resolve(themeFileName);
            
            // Copy theme file
            Files.copy(sourceThemePath, destThemePath, StandardCopyOption.REPLACE_EXISTING);
            logger.debug("Copied theme file: {} -> {}", sourceThemePath, destThemePath);
            
            // Render markdown to HTML if a markdown path is provided (no image dir handling)
            if (theme.getMarkdownPath() != null && !theme.getMarkdownPath().isEmpty()) {
                Path markdownPath = providerDir.resolve(theme.getMarkdownPath());
                if (Files.exists(markdownPath) && Files.isRegularFile(markdownPath)) {
                    try {
                        String htmlBody = markdownRenderer.renderFile(markdownPath);
                        if (htmlBody == null) htmlBody = "";
                        String title = themeId;
                        String fullHtml = markdownRenderer.createHtmlDocument(htmlBody, title, configManager.isDarkMode());
                        String baseName = themeFileName.contains(".") ? themeFileName.substring(0, themeFileName.lastIndexOf('.')) : themeFileName;
                        Path destHtmlPath = customThemesDir.resolve(baseName + ".html");
                        Files.writeString(destHtmlPath, fullHtml, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                        logger.debug("Rendered markdown to HTML: {} -> {}", markdownPath, destHtmlPath);
                    } catch (Exception e) {
                        logger.warn("Failed to render markdown for theme {}: {}", themeId, e.getMessage());
                    }
                } else {
                    logger.debug("Markdown file not found for theme {} at {}", themeId, markdownPath);
                }
            }
            
            return true;
            
        } catch (IOException e) {
            logger.error("Failed to install theme: {}", themeId, e);
            return false;
        }
    }
    
    /**
     * Copy a directory and all its contents recursively
     */
    private void copyDirectory(Path sourceDir, Path destDir) throws IOException {
        Files.createDirectories(destDir);
        
        Files.walk(sourceDir)
             .forEach(sourcePath -> {
                 try {
                     Path destPath = destDir.resolve(sourceDir.relativize(sourcePath));
                     if (Files.isDirectory(sourcePath)) {
                         Files.createDirectories(destPath);
                     } else {
                         Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
                     }
                 } catch (IOException e) {
                     logger.warn("Failed to copy: {} -> {}", sourcePath, destDir, e);
                 }
             });
    }
    
    /**
     * Get available themes from all configured providers
     */
    public List<ThemeInfo> getAvailableThemes() {
        List<ThemeInfo> themes = new ArrayList<>();
        
        String programDir = configManager.getSelectedProgramDir();
        if (programDir.isEmpty()) {
            return themes;
        }
        
        Path programPath = Paths.get(programDir);
        ProgramValidator.ValidationResult validation = ProgramValidator.validateProgramDirectory(programPath);
        
        if (!validation.isValid()) {
            return themes;
        }
        
        Map<String, String> themeProviders = configManager.getInstalledThemeProviders();
        Path providersRoot = configManager.getProvidersRoot();
        
        for (Map.Entry<String, String> provider : themeProviders.entrySet()) {
            String repoName = provider.getValue().substring(provider.getValue().lastIndexOf('/') + 1);
            Path providerDir = providersRoot.resolve(repoName);
            
            if (Files.exists(providerDir)) {
                ThemeIndex themeIndex = indexParser.parseIndex(providerDir);
                if (themeIndex != null) {
                    for (Map.Entry<String, ThemeIndex.ThemeEntry> themeEntry : 
                         themeIndex.getPresentThemes().entrySet()) {
                        ThemeIndex.ThemeEntry te = themeEntry.getValue();
                        String displayName = te.getName() != null ? te.getName() : themeEntry.getKey();
                        themes.add(new ThemeInfo(
                            themeEntry.getKey(),
                            displayName,
                            provider.getKey(),
                            themeIndex.isCertifiedByIvan(),
                            themeIndex.isOfficial(),
                            themeIndex.getTagsList()
                        ));
                    }
                }
            }
        }
        
        return themes;
    }

    /** Provider metadata for UI selection */
    public static class ProviderInfo {
        private final String id;              // providerId from config map key
        private final String repository;      // username/repo
        private final Path providerDir;       // themeProviders/<repo>
        private final ThemeIndex index;       // parsed index.yml
        
        public ProviderInfo(String id, String repository, Path providerDir, ThemeIndex index) {
            this.id = id;
            this.repository = repository;
            this.providerDir = providerDir;
            this.index = index;
        }
        public String getId() { return id; }
        public String getRepository() { return repository; }
        public Path getProviderDir() { return providerDir; }
        public ThemeIndex getIndex() { return index; }
        public Path getIconPath() { return index.getIcon() != null ? providerDir.resolve(index.getIcon()) : null; }
        public Path getHomepagePath() { return index.getHomepage() != null ? providerDir.resolve(index.getHomepage()) : null; }
        public String getDisplayName() { return index.getName() != null ? index.getName() : id; }
    }

    /** List available providers with parsed indexes */
    public List<ProviderInfo> getAvailableProviders() {
        List<ProviderInfo> providers = new ArrayList<>();
        String programDir = configManager.getSelectedProgramDir();
        if (programDir.isEmpty()) return providers;
        Path programPath = Paths.get(programDir);
        ProgramValidator.ValidationResult validation = ProgramValidator.validateProgramDirectory(programPath);
        if (!validation.isValid()) return providers;
        Map<String, String> themeProviders = configManager.getInstalledThemeProviders();
        Path providersRoot = configManager.getProvidersRoot();
        for (Map.Entry<String, String> entry : themeProviders.entrySet()) {
            String providerId = entry.getKey();
            String repoName = entry.getValue().substring(entry.getValue().lastIndexOf('/') + 1);
            Path providerDir = providersRoot.resolve(repoName);
            if (!Files.exists(providerDir)) continue;
            ThemeIndex index = indexParser.parseIndex(providerDir);
            if (index != null) {
                providers.add(new ProviderInfo(providerId, entry.getValue(), providerDir, index));
            }
        }
        return providers;
    }

    /** Install a single theme by provider and theme id */
    public boolean installTheme(String providerId, String themeId) {
        String programDir = configManager.getSelectedProgramDir();
        if (programDir.isEmpty()) return false;
        Path programPath = Paths.get(programDir);
        ProgramValidator.ValidationResult validation = ProgramValidator.validateProgramDirectory(programPath);
        if (!validation.isValid()) return false;
        Map<String, String> themeProviders = configManager.getInstalledThemeProviders();
        String repository = themeProviders.get(providerId);
        if (repository == null) return false;
        String repoName = repository.substring(repository.lastIndexOf('/') + 1);
        Path providerDir = configManager.getProvidersRoot().resolve(repoName);
        if (!Files.exists(providerDir)) return false;
        ThemeIndex themeIndex = indexParser.parseIndex(providerDir);
        if (themeIndex == null) return false;
        ThemeIndex.ThemeEntry theme = themeIndex.getPresentThemes().get(themeId);
        if (theme == null) return false;
        return installSingleTheme(themeId, theme, providerDir, validation.getCustomThemesDir());
    }
    
    /**
     * Information about an available theme
     */
    public static class ThemeInfo {
        private final String id;
        private final String name;
        private final String providerId;
        private final boolean certified;
        private final boolean official;
        private final List<String> tags;
        
        public ThemeInfo(String id, String name, String providerId, 
                        boolean certified, boolean official, List<String> tags) {
            this.id = id;
            this.name = name;
            this.providerId = providerId;
            this.certified = certified;
            this.official = official;
            this.tags = tags;
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public String getProviderId() { return providerId; }
        public boolean isCertified() { return certified; }
        public boolean isOfficial() { return official; }
        public List<String> getTags() { return tags; }
    }
    
    /**
     * Shutdown the executor service
     */
    public void shutdown() {
        executorService.shutdown();
    }

    /** Update all configured providers (git pull if exists, else clone). */
    public boolean updateAllProviders(GitHubHandler.ProgressCallback progressCallback) {
        Map<String, String> themeProviders = configManager.getInstalledThemeProviders();
        Path providersRoot = configManager.getProvidersRoot();
        boolean allOk = true;
        for (Map.Entry<String, String> entry : themeProviders.entrySet()) {
            String repo = entry.getValue();
            try {
                boolean ok = gitHubHandler.cloneRepository(repo, providersRoot, progressCallback);
                allOk &= ok;
            } catch (Exception e) {
                logger.error("Update failed for {}", repo, e);
                allOk = false;
            }
        }
        return allOk;
    }
}