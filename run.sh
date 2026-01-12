#!/bin/bash

# Script to Animation Agent - Quick Start Script

echo "========================================="
echo "Script to Animation Agent"
echo "========================================="
echo ""

# Check if API keys are set
if [ -z "$GEMINI_API_KEY" ]; then
    echo "Warning: GEMINI_API_KEY environment variable is not set"
    echo "Please set it with: export GEMINI_API_KEY='your-key'"
    echo ""
fi

if [ -z "$OPENAI_API_KEY" ]; then
    echo "Warning: OPENAI_API_KEY environment variable is not set"
    echo "Please set it with: export OPENAI_API_KEY='your-key'"
    echo ""
fi

# Check if JAR exists
JAR_FILE="target/script-to-animation-agent-1.0.0.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo "JAR file not found. Building project..."
    mvn clean package
    
    if [ $? -ne 0 ]; then
        echo "Build failed. Please check the errors above."
        exit 1
    fi
fi

# Run the application
echo "Starting Animation Agent..."
echo ""

if [ $# -eq 0 ]; then
    # No arguments - interactive mode
    java -jar "$JAR_FILE"
else
    # Pass all arguments to the application
    java -jar "$JAR_FILE" "$@"
fi
