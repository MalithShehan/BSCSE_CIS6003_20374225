#!/usr/bin/env bash
# ==============================================================================
# Sunrise Dental Clinic Management System - One-Click Start Script
# ==============================================================================

set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TOOLS_DIR="$HOME/.tools"
MYSQL_DIR="$TOOLS_DIR/mysql"
TOMCAT_DIR="$TOOLS_DIR/tomcat10"
JDK_DIR="$TOOLS_DIR/jdk17/Contents/Home"
MAVEN_DIR="$TOOLS_DIR/maven"

export JAVA_HOME="$JDK_DIR"
export PATH="/usr/local/mysql/bin:$JAVA_HOME/bin:$MAVEN_DIR/bin:$MYSQL_DIR/bin:$TOMCAT_DIR/bin:$PATH"

echo "========================================================"
echo "🦷 Starting Sunrise Dental Clinic Management System"
echo "========================================================"

# 1. Start MySQL if not already running
if nc -z 127.0.0.1 3306 >/dev/null 2>&1 || lsof -i :3306 >/dev/null 2>&1; then
    echo "✓ MySQL server is already running on port 3306."
else
    echo "⏳ Starting MySQL Community Server..."
    nohup "$MYSQL_DIR/bin/mysqld" --defaults-file="$MYSQL_DIR/my.cnf" > "$MYSQL_DIR/mysqld.log" 2>&1 &
    
    # Wait for MySQL socket to respond
    for i in {1..30}; do
        if "$MYSQL_DIR/bin/mysqladmin" --defaults-file="$MYSQL_DIR/my.cnf" -u root -p'Malith@1001' ping >/dev/null 2>&1; then
            echo "✓ MySQL server started successfully."
            break
        fi
        sleep 0.5
    done
fi

# 2. Check if WAR needs compiling or is missing
WAR_FILE="$PROJECT_DIR/backend/target/sunrise-dental-backend.war"
if [ ! -f "$WAR_FILE" ]; then
    echo "⏳ Compiling and packaging backend WAR..."
    cd "$PROJECT_DIR/backend"
    mvn package -DskipTests
    cd "$PROJECT_DIR"
fi

# Deploy WAR to Tomcat webapps if needed
mkdir -p "$TOMCAT_DIR/webapps"
cp "$WAR_FILE" "$TOMCAT_DIR/webapps/sunrise-dental-clinic.war"
cp "$WAR_FILE" "$TOMCAT_DIR/webapps/ROOT.war"

# 3. Start Apache Tomcat if not already running
if lsof -i :8080 >/dev/null 2>&1; then
    echo "✓ Apache Tomcat is already running on port 8080."
else
    echo "⏳ Starting Apache Tomcat 10.1..."
    "$TOMCAT_DIR/bin/startup.sh" >/dev/null 2>&1
    
    # Wait for Tomcat to respond
    for i in {1..20}; do
        if curl -s "http://localhost:8080/sunrise-dental-clinic/" >/dev/null 2>&1; then
            echo "✓ Apache Tomcat 10.1 started successfully."
            break
        fi
        sleep 0.5
    done
fi

echo "========================================================"
echo "✨ Sunrise Dental Clinic is running!"
echo "📍 Application URL : http://localhost:8080/sunrise-dental-clinic/"
echo "👥 Demo Logins:"
echo "   - Director / Admin : admin / Admin@123"
echo "   - Receptionist     : receptionist / Reception@123"
echo "   - Dental Surgeon   : dentist / Dentist@123"
echo "========================================================"

# Open in default macOS browser
open "http://localhost:8080/sunrise-dental-clinic/"
