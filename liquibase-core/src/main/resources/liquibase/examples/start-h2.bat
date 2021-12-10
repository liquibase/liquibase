@echo off
if "%OS%" == "Windows_NT" setlocal

setlocal enabledelayedexpansion

rem %~dp0 is expanded pathname of the current script under NT
rem %~p0 is the directory of the current script

if exist %~p0\..\liquibase.jar SET LIQUIBASE_HOME="%~p0\.."

if "%LIQUIBASE_HOME%"=="" (
    FOR /F "tokens=* USEBACKQ" %%g IN (`where liquibase.bat`) do (SET "LIQUIBASE_HOME=%%~dpg")
)

if "%LIQUIBASE_HOME%"=="" (
    echo "Must set LIQUIBASE_HOME environment variable, or have liquibase.bat in your PATH"
    exit /B 1
)

if "%JAVA_HOME%"=="" (

    rem check for jre dir in liquibase_home
    if NOT "%LIQUIBASE_HOME%"=="" if exist "%LIQUIBASE_HOME%\jre" (
        set JAVA_HOME=%LIQUIBASE_HOME%\jre
    )
)

if "%JAVA_HOME%"=="" (
    set JAVA_PATH=java
) else (
    set JAVA_PATH=%JAVA_HOME%\bin\java
)

"%JAVA_PATH%" -cp "%LIQUIBASE_HOME%\lib\h2-1.4.200.jar;%LIQUIBASE_HOME%\liquibase.jar" liquibase.example.StartH2Main
