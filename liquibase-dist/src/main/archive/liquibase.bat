@echo off
if "%OS%" == "Windows_NT" setlocal

setlocal enabledelayedexpansion

rem %~dp0 is expanded pathname of the current script under NT
set LIQUIBASE_HOME="%~dp0"

rem remove quotes around LIQUIBASE_HOME
set LIQUIBASE_HOME=%LIQUIBASE_HOME:"=%

rem remove quotes around JAVA_HOME if set
if NOT "%JAVA_HOME%" == "" set JAVA_HOME=%JAVA_HOME:"=%

rem set JAVA_HOME to local jre dir if not set
if exist "%LIQUIBASE_HOME%\jre" if "%JAVA_HOME%"=="" (
    set JAVA_HOME=%LIQUIBASE_HOME%\jre
)

rem use only the first JAVA_HOME in the semicolon separated list
for /f "tokens=1 delims=;" %%a in ("%JAVA_HOME%") do (
    set JAVA_HOME=%%a
)

rem special characters may be lost
setlocal DISABLEDELAYEDEXPANSION

IF NOT DEFINED JAVA_OPTS set JAVA_OPTS=

set JAVA_PATH=java
if NOT "%JAVA_HOME%" == "" set JAVA_PATH=%JAVA_HOME%\bin\java

"%JAVA_PATH%" %JAVA_OPTS% -jar "%LIQUIBASE_HOME%\liquibase.jar" %*
