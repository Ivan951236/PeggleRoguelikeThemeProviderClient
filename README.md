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
- Peggle Roguelike Preset Generator installation

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

### Installing Themes
1. Click "Install All Themes" to download from all configured providers
2. Progress is shown in real-time with detailed logging
3. Themes are automatically copied to the game's `customThemes` directory

### Adding Theme Providers
1. Click "Add Provider" button
2. Enter GitHub repository in format: `username/repository-name`
3. The repository will be cloned and indexed automatically

### Theme Provider Requirements

Theme providers must include an `index.yml` file with the following structure:

```yaml
present_themes:
  01f5ee5b-d823-4abd-bcb3-d56b1cdc7163: themes\light\gameboy-green-light.yml
    images_dir: images\01f5ee5b-d823-4abd-bcb3-d56b1cdc7163
  449b4817-c6e7-480b-bdb4-df44085cdd6d: themes\dark\gameboy-green-dark.yml
    images_dir: images\449b4817-c6e7-480b-bdb4-df44085cdd6d
 
theme_provider: official_ivan
for_program: prpg
desc: "The Official Theme provider that must be installed by default in clients"
certified_by_ivan: true
theme_format: yml
date_created: 23/10/2025
theme_provider_type: official
tags: "nsfw_images_allowed, moonandsun, official, prpg"
```

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
├── customThemes/           # Game themes installed here
│   ├── theme1.yml
│   ├── theme2.yml
│   └── images/
└── themeProviders/         # Cloned repositories
    └── PeggleRoguelikeThemes/
        ├── index.yml
        ├── themes/
        └── images/
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
