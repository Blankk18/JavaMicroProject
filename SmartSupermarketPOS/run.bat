@echo off
echo ==========================================
echo   Smart Supermarket POS - Launcher
echo ==========================================

set JAVA_HOME=C:\Program Files\Java\jdk-26.0.1
set JAVAC=%JAVA_HOME%\bin\javac.exe
set JAVA=%JAVA_HOME%\bin\java.exe
set SRC_DIR=src\main\java\com\smartpos
set OUT_DIR=target\classes
set MYSQL_JAR=C:\Program Files\Java\mysql-connector-j-9.7.0\mysql-connector-j-9.7.0.jar
set CP=%OUT_DIR%;%MYSQL_JAR%

if not exist "%JAVAC%" (
    echo ERROR: JDK not found at %JAVA_HOME%
    echo Please update JAVA_HOME in this script.
    pause
    exit /b 1
)

echo [1/3] Compiling...
if not exist %OUT_DIR% mkdir %OUT_DIR%

REM Copy resources (images, etc.)
if exist src\main\resources xcopy /E /Y /Q src\main\resources\* %OUT_DIR%\

REM Compile all .java files targeting Java 17 (runs on any Java 17+ JRE)
for /f "delims=" %%f in ('dir /b /s "%SRC_DIR%\*.java"') do (
    "%JAVAC%" -encoding UTF-8 --release 21 -cp "%CP%" -d "%OUT_DIR%" "%%f"
    if errorlevel 1 (
        echo.
        echo ERROR: Compilation failed for %%f
        pause
        exit /b 1
    )
)

echo [2/3] Compiled successfully!
echo [3/3] Launching Smart Supermarket POS...
echo.
"%JAVA%" -cp "%CP%" com.smartpos.Main
pause
