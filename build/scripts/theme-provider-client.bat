@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  theme-provider-client startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and THEME_PROVIDER_CLIENT_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\theme-provider-client-1.0.0.jar;%APP_HOME%\lib\org.eclipse.jgit.ssh.jsch-6.7.0.202309050840-r.jar;%APP_HOME%\lib\org.eclipse.jgit-6.7.0.202309050840-r.jar;%APP_HOME%\lib\jackson-databind-2.15.2.jar;%APP_HOME%\lib\jackson-core-2.15.2.jar;%APP_HOME%\lib\jackson-annotations-2.15.2.jar;%APP_HOME%\lib\jackson-dataformat-yaml-2.15.2.jar;%APP_HOME%\lib\snakeyaml-2.2.jar;%APP_HOME%\lib\javafx-fxml-21.0.1-win.jar;%APP_HOME%\lib\atlantafx-base-2.0.1.jar;%APP_HOME%\lib\javafx-controls-21.0.1-win.jar;%APP_HOME%\lib\ikonli-javafx-12.3.1.jar;%APP_HOME%\lib\ikonli-material2-pack-12.3.1.jar;%APP_HOME%\lib\ikonli-materialdesign2-pack-12.3.1.jar;%APP_HOME%\lib\logback-classic-1.4.11.jar;%APP_HOME%\lib\slf4j-api-2.0.9.jar;%APP_HOME%\lib\flexmark-all-0.64.8.jar;%APP_HOME%\lib\JavaEWAH-1.2.3.jar;%APP_HOME%\lib\commons-codec-1.16.0.jar;%APP_HOME%\lib\jsch-0.1.55.jar;%APP_HOME%\lib\jzlib-1.1.3.jar;%APP_HOME%\lib\javafx-graphics-21.0.1-win.jar;%APP_HOME%\lib\ikonli-core-12.3.1.jar;%APP_HOME%\lib\logback-core-1.4.11.jar;%APP_HOME%\lib\flexmark-profile-pegdown-0.64.8.jar;%APP_HOME%\lib\flexmark-ext-abbreviation-0.64.8.jar;%APP_HOME%\lib\flexmark-ext-admonition-0.64.8.jar;%APP_HOME%\lib\flexmark-ext-anchorlink-0.64.8.jar;%APP_HOME%\lib\flexmark-ext-aside-0.64.8.jar;%APP_HOME%\lib\flexmark-ext-enumerated-reference-0.64.8.jar;%APP_HOME%\lib\flexmark-ext-attributes-0.64.8.jar;%APP_HOME%\lib\flexmark-ext-autolink-0.64.8.jar;%APP_HOME%\lib\flexmark-ext-definition-0.64.8.jar;%APP_HOME%\lib\flexmark-html2md-converter-0.64.8.jar;%APP_HOME%\lib\flexmark-ext-emoji-0.64.8.jar;%APP_HOME%\lib\flexmark-ext-escaped-character-0.64.8.jar;%APP_HOME%\lib\flexmark-ext-footnotes-0.64.8.jar;%APP_HOME%\lib\flexmark-ext-gfm-issues-0.64.8.jar;%APP_HOME%\lib\flexmark-jira-converter-0.64.8.jar;%APP_HOME%\lib\flexmark-youtrack-converter-0.64.8.jar;%APP_HOME%\lib\flexmark-ext-gfm-strikethrough-0.64.8.jar;%APP_HOME%\lib\flexmark-ext-gfm-tasklist-0.64.8.jar;%APP_HOME%\lib\flexmark-ext-gfm-users-0.64.8.jar;%APP_HOME%\lib\flexmark-ext-macros-0.64.8.jar;%APP_HOME%\lib\flexmark-ext-gitlab-0.64.8.jar;%APP_HOME%\lib\flexmark-ext-jekyll-front-matter-0.64.8.jar;%APP_HOME%\lib\flexmark-ext-jekyll-tag-0.64.8.jar;%APP_HOME%\lib\flexmark-ext-media-tags-0.64.8.jar;%APP_HOME%\lib\flexmark-ext-resizable-image-0.64.8.jar;%APP_HOME%\lib\flexmark-ext-ins-0.64.8.jar;%APP_HOME%\lib\flexmark-ext-xwiki-macros-0.64.8.jar;%APP_HOME%\lib\flexmark-ext-superscript-0.64.8.jar;%APP_HOME%\lib\flexmark-ext-tables-0.64.8.jar;%APP_HOME%\lib\flexmark-ext-toc-0.64.8.jar;%APP_HOME%\lib\flexmark-ext-typographic-0.64.8.jar;%APP_HOME%\lib\flexmark-ext-wikilink-0.64.8.jar;%APP_HOME%\lib\flexmark-ext-yaml-front-matter-0.64.8.jar;%APP_HOME%\lib\flexmark-ext-youtube-embedded-0.64.8.jar;%APP_HOME%\lib\flexmark-pdf-converter-0.64.8.jar;%APP_HOME%\lib\flexmark-0.64.8.jar;%APP_HOME%\lib\flexmark-util-0.64.8.jar;%APP_HOME%\lib\flexmark-util-format-0.64.8.jar;%APP_HOME%\lib\flexmark-util-ast-0.64.8.jar;%APP_HOME%\lib\flexmark-util-builder-0.64.8.jar;%APP_HOME%\lib\flexmark-util-dependency-0.64.8.jar;%APP_HOME%\lib\flexmark-util-html-0.64.8.jar;%APP_HOME%\lib\flexmark-util-options-0.64.8.jar;%APP_HOME%\lib\flexmark-util-sequence-0.64.8.jar;%APP_HOME%\lib\flexmark-util-collection-0.64.8.jar;%APP_HOME%\lib\flexmark-util-data-0.64.8.jar;%APP_HOME%\lib\flexmark-util-misc-0.64.8.jar;%APP_HOME%\lib\flexmark-util-visitor-0.64.8.jar;%APP_HOME%\lib\javafx-base-21.0.1-win.jar;%APP_HOME%\lib\autolink-0.6.0.jar;%APP_HOME%\lib\jsoup-1.15.4.jar;%APP_HOME%\lib\icu4j-72.1.jar;%APP_HOME%\lib\openhtmltopdf-pdfbox-1.0.10.jar;%APP_HOME%\lib\openhtmltopdf-rtl-support-1.0.10.jar;%APP_HOME%\lib\openhtmltopdf-core-1.0.10.jar;%APP_HOME%\lib\annotations-24.0.1.jar;%APP_HOME%\lib\graphics2d-0.32.jar;%APP_HOME%\lib\pdfbox-2.0.24.jar;%APP_HOME%\lib\xmpbox-2.0.24.jar;%APP_HOME%\lib\fontbox-2.0.24.jar;%APP_HOME%\lib\commons-logging-1.2.jar


@rem Execute theme-provider-client
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %THEME_PROVIDER_CLIENT_OPTS%  -classpath "%CLASSPATH%" com.ivan.themeprovider.ThemeProviderClient %*

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem Set variable THEME_PROVIDER_CLIENT_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%THEME_PROVIDER_CLIENT_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
