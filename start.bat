@echo off
echo Starting CTH Data Reconciliation Platform with DB2 for AS400 support and -Xmx8g max heap size...
if not exist "target\app.jar" (
    echo target\app.jar not found. Running build.bat first...
    call build.bat
)
start "CTH-App" java -Xmx8g -jar target\app.jar
echo Application started on http://localhost:8080
