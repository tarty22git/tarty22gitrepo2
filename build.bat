@echo off
echo ===================================================
echo  Building CTH Data Reconciliation Platform (Windows)
echo ===================================================
call mvn clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Build failed!
    exit /b %ERRORLEVEL%
)
echo [SUCCESS] Package created successfully at target/app.jar
