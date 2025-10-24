# Peggle Roguelike Theme Provider Client

A Java application for automatically downloading and installing themes from GitHub repositories for the Peggle Roguelike Generator.

## Features

- **Material Design 3 UI** with dark/light mode support
- **Automatic theme detection** from GitHub repositories 
- **Program validation** to ensure correct Peggle Roguelike Generator installation
- **Theme installation** directly to the game's customThemes directory
- **Configuration management** with YAML-based settings
- **Pre-installed official theme provider** (Ivan951236/PeggleRoguelikeThemes)
- **Certified theme verification** with Material Design icons
- **Progress tracking** for downloads and installations
- **Comprehensive logging** for troubleshooting

## Requirements

- Java 17 or higher
- JavaFX 21+ (included in dependencies)
- Internet connection for GitHub access
- Peggle Roguelike Generator installation

## Installation

1. Clone this repository or download the source code
2. Build using Gradle:
   ```bash
   ./gradlew build
   ```
3. Run the application:
   ```bash
   ./gradlew run
   ```

## Usage

### First Run
1. Launch the application
2. Select your Peggle Roguelike Generator directory (containing `peggle-roguelike-generator*.jar`)
3. The app will automatically create missing directories (`customThemes`, `themeProviders`)
4. You will see the Theme Provider selection screen (with icons and names). Click a provider to view its homepage.

### Installing Themes
- From a provider homepage, click "All Themes" to open the catalog.
- Click a theme to view its details; the description is rendered from the theme's markdown file.
- Use the "Install" button (top-right) to install just that theme to `customThemes`.

### Adding Theme Providers
1. Click "Add Provider" button
2. Enter GitHub repository in format: `username/repository-name`
3. The repository will be cloned and indexed automatically

### Theme Provider Requirements

Theme providers must include an `index.yml` file with the following structure (updated):

```yaml
# Provider metadata (required)
name: Blue Forest Themes              # Friendly provider name (REQUIRED)
theme_provider: blue_forest          # Provider ID (unique)
for_program: prpg                    # Target program ID
desc: "Nature-inspired light/dark themes"
certified_by_ivan: false             # or true
theme_format: yml
date_created: 24/10/2025
theme_provider_type: community       # official | community
tags: "nature, calming, prpg"        # Provider-level tags (not per-theme)
icon: assets/icon.png                # PNG/JPG/WEBP/GIF supported
homepage: homepage.md                # Provider homepage markdown file

# Catalog of themes (REQUIRED)
present_themes:
  3a9a2e5c-4d6b-4b1c-9b1b-9a2e5c4d6b4b:
    name: Blue Sky Light
    category: light                   # dark | light | other
    theme: themes/light/blue-sky-light.yml
    markdown: themes/light/blue-sky-light.md
    theme_tags: ["blue", "sky", "light"]

  b7c1d9e2-8f3a-4c6d-9e1b-2a3c4d5e6f7a:
    name: Forest Green Dark
    category: dark
    theme: themes/dark/forest-green-dark.yml
    markdown: themes/dark/forest-green-dark.md
    theme_tags: ["green", "forest", "dark"]
```

Notes:
- images_dir is no longer used; use markdown files for descriptions and relative assets.
- The provider homepage must be a markdown file referenced by `homepage`.
- Icons can be PNG/JPG/GIF/WEBP; WEBP is supported via embedded WebView.
- Per-theme tags are in `theme_tags`; provider-level `tags` apply to the provider only.

## Configuration

The application stores its configuration in:
- Windows: `%USERPROFILE%\.theme_provider_client\theme_provider_config.yml`
- Linux/Mac: `~/.theme_provider_client/theme_provider_config.yml`

Example configuration:
```yaml
selected_program_dir: "C:\Games\PeggleRoguelike"
installed_theme_providers:
  official_ivan: Ivan951236/PeggleRoguelikeThemes
dark_mode: true
auto_update_themes: true
```

## Architecture

### Core Components

- **ConfigManager**: YAML-based configuration storage and management
- **ProgramValidator**: Validates Peggle Roguelike Generator installation
- **GitHubHandler**: JGit-based repository cloning and updates
- **ThemeIndexParser**: SnakeYAML-based parsing of theme provider indexes
- **ThemeInstaller**: Theme download and installation management
- **MainWindow**: JavaFX UI with Material Design 3 theming

### Directory Structure

```
PeggleRoguelikeDirectory/
├── peggle-roguelike-generator.jar
├── customThemes/                    # Game themes installed here
│   ├── <theme>.yml                  # Copied theme files
│   └── <theme>.html                 # Auto-generated HTML from theme markdown (for preview)
└── themeProviders/                  # Cloned repositories (GitHub)
    └── <RepoName>/
        ├── index.yml                # Uses updated structure (see above)
        ├── homepage.md              # Provider homepage markdown
        ├── assets/
        │   └── icon.png             # Provider icon (png/jpg/webp/gif)
        └── themes/
            ├── light/
            │   ├── blue-sky-light.yml
            │   └── blue-sky-light.md
            └── dark/
                ├── forest-green-dark.yml
                └── forest-green-dark.md
```

## Development

### Building
```bash
./gradlew build
```

### Running
```bash
./gradlew run
```

### Creating Distribution
```bash
./gradlew jar
```

### Dependencies
- **JGit**: GitHub repository operations
- **SnakeYAML**: YAML file parsing
- **JavaFX**: Modern UI framework
- **AtlantaFX**: Material Design 3 theming
- **Ikonli**: Material Design icons
- **SLF4J + Logback**: Logging framework

## Troubleshooting

### Common Issues

1. **"No JAR file found"**: Ensure the directory contains a file starting with `peggle-roguelike-generator` and ending with `.jar`

2. **"Failed to clone repository"**: Check internet connection and repository URL format (`username/reponame`)

3. **"Theme installation failed"**: Verify write permissions to the program directory

### Logs
Application logs are stored in:
- Windows: `%USERPROFILE%\.theme_provider_client\logs\`
- Linux/Mac: `~/.theme_provider_client/logs/`

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is open source. See LICENSE file for details.

## Credits

- Material Design 3 theming by AtlantaFX
- Icons by Material Design Icons
- Built with JavaFX and Gradle