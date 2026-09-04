#!/usr/bin/env bash
# ==============================================================================
# Sunrise Dental Clinic Management System - Graceful Stop Script
# ==============================================================================

TOOLS_DIR="$HOME/.tools"
MYSQL_DIR="$TOOLS_DIR/mysql"
TOMCAT_DIR="$TOOLS_DIR/tomcat10"
JDK_DIR="$TOOLS_DIR/jdk17/Contents/Home"

export JAVA_HOME="$JDK_DIR"

echo "========================================================"
echo "🛑 Stopping Sunrise Dental Clinic Services"
echo "========================================================"

# 1. Stop Apache Tomcat
if lsof -i :8080 >/dev/null 2>&1; then
    echo "⏳ Stopping Apache Tomcat..."
    "$TOMCAT_DIR/bin/shutdown.sh" >/dev/null 2>&1 || true
    sleep 2
    if lsof -i :8080 >/dev/null 2>&1; then
        kill -9 $(lsof -t -i :8080) 2>/dev/null || true
    fi
    echo "✓ Apache Tomcat stopped."
else
    echo "✓ Apache Tomcat was not running."
fi

# 2. Stop MySQL
if nc -z 127.0.0.1 3306 >/dev/null 2>&1 || lsof -i :3306 >/dev/null 2>&1; then
    echo "⏳ Stopping MySQL Community Server..."
    MYSQLADMIN_CMD="mysqladmin"
    if command -v /usr/local/mysql/bin/mysqladmin >/dev/null 2>&1; then
        MYSQLADMIN_CMD="/usr/local/mysql/bin/mysqladmin"
    elif [ -f "$MYSQL_DIR/bin/mysqladmin" ]; then
        MYSQLADMIN_CMD="$MYSQL_DIR/bin/mysqladmin"
    fi
    "$MYSQLADMIN_CMD" -u root -p'Malith@1001' shutdown >/dev/null 2>&1 || true
    sleep 2
    if lsof -i :3306 >/dev/null 2>&1; then
        kill -9 $(lsof -t -i :3306) 2>/dev/null || true
    fi
    echo "✓ MySQL server stopped."
else
    echo "✓ MySQL server was not running."
fi

echo "========================================================"
echo "✓ All clinic services stopped cleanly."
echo "========================================================"
