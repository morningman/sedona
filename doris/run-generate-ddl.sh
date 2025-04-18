#!/bin/bash

# Get the directory where the script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

# Default jar file path
DEFAULT_JAR="sedona-doris-1.8.0-SNAPSHOT.jar"
JAR_PATH="$DEFAULT_JAR"

# Process command line arguments
ARGS=()
while [[ $# -gt 0 ]]; do
    case $1 in
        --jarpath)
            JAR_PATH="$2"
            shift 2
            ;;
        *)
            ARGS+=("$1")
            shift
            ;;
    esac
done

# Set the classpath
CLASSPATH="$SCRIPT_DIR/target/$JAR_PATH"

# Check if output file parameter is provided
if [ ${#ARGS[@]} -eq 0 ]; then
    # If no arguments, run the program directly
    java -cp "$CLASSPATH" org.apache.sedona.doris.dorissql.tools.GenerateDorisDDL
else
    # If arguments are provided, pass them to the program
    java -cp "$CLASSPATH" org.apache.sedona.doris.dorissql.tools.GenerateDorisDDL "${ARGS[@]}"
fi 
