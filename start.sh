#!/bin/bash
echo "Starting CTH Data Reconciliation Platform with DB2 for AS400 support and -Xmx8g max heap size..."
if [ ! -f "target/app.jar" ]; then
    echo "target/app.jar not found. Running build.sh first..."
    ./build.sh
fi
nohup java -Xmx8g -jar target/app.jar > app.log 2>&1 &
echo $! > app.pid
echo "Application started in background (PID: $(cat app.pid)). Logs at app.log"
