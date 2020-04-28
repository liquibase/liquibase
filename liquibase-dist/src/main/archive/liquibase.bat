@echo off
if "%OS%" == "Windows_NT" setlocal

setlocal enabledelayedexpansion

rem %~dp0 is expanded pathname of the current script under NT
set LIQUIBASE_HOME="%~dp0"

set CP=.
for /R %LIQUIBASE_HOME% %%f in (liquibase*.jar) do set CP=!CP!;%%f
for /R %LIQUIBASE_HOME%\lib %%f in (*.jar) do set CP=!CP!;%%f

rem remove quotes around LIQUIBASE_HOME
set LIQUIBASE_HOME=%LIQUIBASE_HOME:"=%

rem set JAVA_HOME to local jre dir if not set
if exist "%LIQUIBASE_HOME%\jre" if "%JAVA_HOME%"=="" (
    set JAVA_HOME=%LIQUIBASE_HOME%\jre
)


rem add the lib directory itself to the classpath
set CP=!CP!;!LIQUIBASE_HOME!lib

rem special characters may be lost
setlocal DISABLEDELAYEDEXPANSION

IF NOT DEFINED JAVA_OPTS set JAVA_OPTS=

set JAVA_PATH=""
if NOT "%JAVA_HOME%" == "" set JAVA_PATH=%JAVA_HOME%\bin\

"%JAVA_PATH%\java" -cp "%CP%" %JAVA_OPTS% liquibase.integration.commandline.Main %*
