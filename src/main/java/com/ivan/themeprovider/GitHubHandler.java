package com.ivan.themeprovider;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Handles GitHub repository operations using JGit
 */
public class GitHubHandler {
    private static final Logger logger = LoggerFactory.getLogger(GitHubHandler.class);
    private static final String GITHUB_BASE_URL = "https://github.com/";
    
    /**
     * Progress callback for repository operations
     */
    public interface ProgressCallback {
        void onProgress(String task, int completed, int total);
        void onMessage(String message);
    }
    
    /**
     * Clone a GitHub repository to the specified directory
     * 
     * @param repositoryName Repository in format "username/reponame"
     * @param targetDirectory Where to clone the repository
     * @param progressCallback Optional progress callback
     * @return true if successful, false otherwise
     */
    public boolean cloneRepository(String repositoryName, Path targetDirectory, 
                                  ProgressCallback progressCallback) {
        
        if (repositoryName == null || repositoryName.trim().isEmpty()) {
            logger.error("Repository name cannot be empty");
            return false;
        }
        
        // Ensure the repository name is in the correct format
        if (!repositoryName.contains("/")) {
            logger.error("Repository name must be in format 'username/reponame': {}", repositoryName);
            return false;
        }
        
        String githubUrl = GITHUB_BASE_URL + repositoryName + ".git";
        String repoName = extractRepositoryName(repositoryName);
        Path cloneDirectory = targetDirectory.resolve(repoName);
        
        // Check if directory already exists
        if (Files.exists(cloneDirectory)) {
            logger.info("Repository directory already exists, attempting to update: {}", cloneDirectory);
            return updateRepository(cloneDirectory, progressCallback);
        }
        
        try {
            // Ensure parent directory exists
            Files.createDirectories(targetDirectory);
            
            logger.info("Cloning repository {} to {}", githubUrl, cloneDirectory);
            
            if (progressCallback != null) {
                progressCallback.onMessage("Cloning " + repositoryName + "...");
            }
            
            Git.cloneRepository()
                .setURI(githubUrl)
                .setDirectory(cloneDirectory.toFile())
                .setProgressMonitor(createProgressMonitor(progressCallback))
                .call()
                .close();
            
            logger.info("Successfully cloned repository: {}", repositoryName);
            
            if (progressCallback != null) {
                progressCallback.onMessage("Successfully cloned " + repositoryName);
            }
            
            return true;
            
        } catch (GitAPIException e) {
            logger.error("Failed to clone repository: {}", repositoryName, e);
            
            if (progressCallback != null) {
                progressCallback.onMessage("Failed to clone " + repositoryName + ": " + e.getMessage());
            }
            
            // Clean up partial clone if it exists
            if (Files.exists(cloneDirectory)) {
                try {
                    deleteDirectory(cloneDirectory);
                } catch (IOException cleanupError) {
                    logger.warn("Failed to clean up partial clone directory", cleanupError);
                }
            }
            
            return false;
        } catch (IOException e) {
            logger.error("IO error while cloning repository: {}", repositoryName, e);
            
            if (progressCallback != null) {
                progressCallback.onMessage("IO error while cloning " + repositoryName);
            }
            
            return false;
        }
    }
    
    /**
     * Update an existing repository
     * 
     * @param repositoryDirectory The directory containing the cloned repository
     * @param progressCallback Optional progress callback
     * @return true if successful, false otherwise
     */
    public boolean updateRepository(Path repositoryDirectory, ProgressCallback progressCallback) {
        if (!Files.exists(repositoryDirectory) || !Files.isDirectory(repositoryDirectory)) {
            logger.error("Repository directory does not exist: {}", repositoryDirectory);
            return false;
        }
        
        try {
            logger.info("Updating repository: {}", repositoryDirectory);
            
            if (progressCallback != null) {
                progressCallback.onMessage("Updating " + repositoryDirectory.getFileName() + "...");
            }
            
            try (Git git = Git.open(repositoryDirectory.toFile())) {
                git.pull()
                   .setProgressMonitor(createProgressMonitor(progressCallback))
                   .call();
                
                logger.info("Successfully updated repository: {}", repositoryDirectory);
                
                if (progressCallback != null) {
                    progressCallback.onMessage("Successfully updated " + repositoryDirectory.getFileName());
                }
                
                return true;
            }
            
        } catch (GitAPIException e) {
            logger.error("Failed to update repository: {}", repositoryDirectory, e);
            
            if (progressCallback != null) {
                progressCallback.onMessage("Failed to update repository: " + e.getMessage());
            }
            
            return false;
        } catch (IOException e) {
            logger.error("IO error while updating repository: {}", repositoryDirectory, e);
            
            if (progressCallback != null) {
                progressCallback.onMessage("IO error while updating repository");
            }
            
            return false;
        }
    }
    
    /**
     * Extract the repository name from a full repository identifier
     * 
     * @param repositoryName Repository in format "username/reponame"
     * @return Just the repository name part
     */
    private String extractRepositoryName(String repositoryName) {
        int slashIndex = repositoryName.lastIndexOf('/');
        if (slashIndex >= 0 && slashIndex < repositoryName.length() - 1) {
            return repositoryName.substring(slashIndex + 1);
        }
        return repositoryName; // Fallback to full name if parsing fails
    }
    
    /**
     * Create a progress monitor for JGit operations
     */
    private ProgressMonitor createProgressMonitor(ProgressCallback progressCallback) {
        if (progressCallback == null) {
            return new ProgressMonitor() {
                @Override
                public void start(int totalTasks) {}
                
                @Override
                public void beginTask(String title, int totalWork) {}
                
                @Override
                public void update(int completed) {}
                
                @Override
                public void endTask() {}
                
                @Override
                public boolean isCancelled() {
                    return false;
                }
                
                @Override
                public void showDuration(boolean enabled) {}
            };
        }
        
        return new ProgressMonitor() {
            private String currentTask;
            private int totalWork;
            private int completedWork;
            
            @Override
            public void start(int totalTasks) {
                // Implementation not needed for our use case
            }
            
            @Override
            public void beginTask(String title, int totalWork) {
                this.currentTask = title;
                this.totalWork = totalWork;
                this.completedWork = 0;
                progressCallback.onProgress(title, 0, totalWork);
            }
            
            @Override
            public void update(int completed) {
                this.completedWork += completed;
                if (currentTask != null) {
                    progressCallback.onProgress(currentTask, completedWork, totalWork);
                }
            }
            
            @Override
            public void endTask() {
                if (currentTask != null && totalWork > 0) {
                    progressCallback.onProgress(currentTask, totalWork, totalWork);
                }
            }
            
            @Override
            public boolean isCancelled() {
                return false; // We don't support cancellation for now
            }
            
            @Override
            public void showDuration(boolean enabled) {
                // Implementation not needed for our use case
            }
        };
    }
    
    /**
     * Delete a directory and all its contents recursively
     */
    private void deleteDirectory(Path directory) throws IOException {
        if (Files.exists(directory)) {
            Files.walk(directory)
                 .sorted((a, b) -> b.compareTo(a)) // Delete files before directories
                 .forEach(path -> {
                     try {
                         Files.delete(path);
                     } catch (IOException e) {
                         logger.warn("Failed to delete: {}", path, e);
                     }
                 });
        }
    }
    
    /**
     * Check if a directory contains a valid Git repository
     */
    public boolean isGitRepository(Path directory) {
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            return false;
        }
        
        try (Git git = Git.open(directory.toFile())) {
            return git.getRepository().getObjectDatabase().exists();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get the remote URL of a Git repository
     */
    public String getRemoteUrl(Path repositoryDirectory) {
        try (Git git = Git.open(repositoryDirectory.toFile())) {
            return git.getRepository()
                     .getConfig()
                     .getString("remote", "origin", "url");
        } catch (Exception e) {
            logger.debug("Failed to get remote URL for: {}", repositoryDirectory, e);
            return null;
        }
    }
}