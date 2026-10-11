#!/bin/bash
echo "Stopping CTH Data Reconciliation Platform..."
if [ -f "app.pid" ]; then
    PID=$(cat app.pid)
    kill $PID 2>/nul || true
    rm -f app.pid
    echo "Stopped process $PID"
else
    pkill -f "target/app.jar" 2>/dev/null || true
    echo "Application stopped."
fi
