@echo off
if "%OS%" == "Windows_NT" setlocal

setlocal enabledelayedexpansion

rem %~dp0 is expanded pathname of the current script under NT
set LIQUIBASE_HOME="%~dp0"

rem remove quotes around LIQUIBASE_HOME
set LIQUIBASE_HOME=%LIQUIBASE_HOME:"=%

rem remove quotes around JAVA_HOME if set
if DEFINED JAVA_HOME if NOT JAVA_HOME == "" set JAVA_HOME=%JAVA_HOME:"=%

rem set JAVA_HOME to local jre dir if not set
if exist "%LIQUIBASE_HOME%\jre" if "%JAVA_HOME%"=="" (
    set JAVA_HOME="%LIQUIBASE_HOME%jre"

    rem remove quotes around JAVA_HOME
    set JAVA_HOME=!JAVA_HOME:"=!
)

if NOT "%JAVA_HOME%" == "" if not exist "%JAVA_HOME%" (
  echo ERROR: The JAVA_HOME environment variable is not defined correctly, so Liquibase cannot be started. JAVA_HOME is set to "%JAVA_HOME%" and it does not exist. >&2
  exit /B 1
)

rem special characters may be lost
setlocal DISABLEDELAYEDEXPANSION

IF NOT DEFINED JAVA_OPTS set JAVA_OPTS=

set JAVA_PATH=java
if NOT "%JAVA_HOME%" == "" set JAVA_PATH=%JAVA_HOME%\bin\java

rem Check Java version
call :check_java_version "%JAVA_PATH%"
if errorlevel 1 exit /b 1

"%JAVA_PATH%" %JAVA_OPTS% -jar "%LIQUIBASE_HOME%\internal\lib\liquibase-core.jar" %*
goto :eof

:check_java_version
setlocal enabledelayedexpansion
set "JAVA_VERSION="
set "JAVA_MAJOR_VERSION="

:: Get the Java version output and look for the first line containing "version"
for /f "tokens=*" %%i in ('"%~1" -version 2^>^&1') do (
    set "line=%%i"
    if "!JAVA_VERSION!"=="" (
        :: Look for the line containing "version" (case insensitive)
        echo !line! | find /i "version" >nul
        if !errorlevel! equ 0 (
            :: Parse the version line by tokens
            for /f "tokens=1,2,3,4,5,6 delims= " %%a in ("!line!") do (
                if "%%b"=="version" (
                    :: The version should be in %%c with quotes
                    set "temp_version=%%c"
                    :: Remove quotes
                    set "temp_version=!temp_version:"=!"
                    set "JAVA_VERSION=!temp_version!"
                )
            )
        )
    )
)

:: Check if we successfully extracted the version
if "!JAVA_VERSION!"=="" (
    echo ERROR: Unable to determine Java version. >&2
    endlocal
    exit /b 1
)

:: Extract the major version
for /f "tokens=1 delims=." %%a in ("!JAVA_VERSION!") do set "JAVA_MAJOR_VERSION=%%a"

:: Handle cases like "1.8.0_..." vs "11.0.0_..."
if "!JAVA_MAJOR_VERSION!"=="1" (
    :: For older Java versions (e.g., Java 8), the major version is "1.x"
    for /f "tokens=2 delims=." %%a in ("!JAVA_VERSION!") do set "JAVA_MAJOR_VERSION=%%a"
)

:: Check if we successfully extracted the major version
if "!JAVA_MAJOR_VERSION!"=="" (
    echo ERROR: Unable to determine Java major version from: !JAVA_VERSION! >&2
    endlocal
    exit /b 1
)

:: Check if Java version is less than 17
if !JAVA_MAJOR_VERSION! LSS 17 (
    echo ERROR: The JVM version is !JAVA_MAJOR_VERSION!. Liquibase requires Java version 17 or higher. >&2
    echo Please install Java 17 or higher, or set JAVA_HOME to a Java 17+ installation. >&2
    endlocal
    exit /b 1
)

endlocal
exit /b 0
