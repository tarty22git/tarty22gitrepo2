#!/bin/bash
set -e
echo "==================================================="
echo " Building CTH Data Reconciliation Platform (Linux) "
echo "==================================================="
mvn clean package -DskipTests
echo "[SUCCESS] Package created successfully at target/app.jar"
