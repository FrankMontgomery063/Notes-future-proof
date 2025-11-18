#!/bin/bash

# Personal Notes Manager - Java CLI Runner Script
# This script builds and runs the notes CLI application

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Project directory (where this script is located)
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR_FILE="$PROJECT_DIR/target/notes-cli.jar"

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Java is installed and version is 11+
check_java() {
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed or not in PATH"
        print_error "Please install Java 11 or higher"
        exit 1
    fi
    
    # Check Java version
    java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d. -f1)
    if [ "$java_version" -lt 11 ]; then
        print_error "Java version is $java_version, but Java 11 or higher is required"
        exit 1
    fi
    
    print_status "Java version check passed"
}

# Check if Maven is installed
check_maven() {
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed or not in PATH"
        print_error "Please install Maven 3.6 or higher"
        exit 1
    fi
    
    print_status "Maven found"
}

# Build the project if JAR doesn't exist or source is newer
build_if_needed() {
    local needs_build=false
    
    if [ ! -f "$JAR_FILE" ]; then
        print_status "JAR file not found, building project..."
        needs_build=true
    else
        # Check if any source files are newer than the JAR
        if find "$PROJECT_DIR/src" -name "*.java" -newer "$JAR_FILE" 2>/dev/null | grep -q .; then
            print_status "Source files have changed, rebuilding..."
            needs_build=true
        fi
    fi
    
    if [ "$needs_build" = true ]; then
        print_status "Building project with Maven..."
        cd "$PROJECT_DIR" || exit 1
        
        if mvn clean package -q; then
            print_status "Build completed successfully"
        else
            print_error "Build failed. Please check the error messages above."
            exit 1
        fi
    fi
}

# Create notes directory if it doesn't exist
ensure_notes_dir() {
    local notes_dir="$PROJECT_DIR/notes"
    if [ ! -d "$notes_dir" ]; then
        print_status "Creating notes directory at $notes_dir"
        mkdir -p "$notes_dir"
    fi
}

# Main execution
main() {
    # Change to project directory
    cd "$PROJECT_DIR" || exit 1
    
    # Perform checks
    check_java
    check_maven
    
    # Build if needed
    build_if_needed
    
    # Ensure notes directory exists
    ensure_notes_dir
    
    # Run the application
    if [ ! -f "$JAR_FILE" ]; then
        print_error "JAR file not found after build: $JAR_FILE"
        exit 1
    fi
    
    # Execute the Java application with all passed arguments
    java -jar "$JAR_FILE" "$@"
}

# Help function
show_help() {
    cat << EOF
Personal Notes Manager - Java CLI

Usage: $0 [command] [options]

Commands:
  create              Create a new note
  list                List all notes or filter by tags
  read <note-id>      Display a specific note
  edit <note-id>      Edit a specific note
  delete <note-id>    Delete a specific note
  search <query>      Search notes by content, title, or tags
  stats               Display statistics about your notes
  --help              Show this help message
  --version           Show version information

Examples:
  $0 create --title "My New Note"
  $0 list --tag programming
  $0 search "java tutorial"
  $0 read my-note-123456
  
For more detailed help on each command:
  $0 <command> --help

Build Commands:
  $0 --build          Force rebuild the project
  $0 --clean          Clean build artifacts and rebuild

Environment:
  Notes are stored in: $PROJECT_DIR/notes/
  Java version required: 11+
  Maven version required: 3.6+
EOF
}

# Handle special arguments
case "$1" in
    --help|-h)
        show_help
        exit 0
        ;;
    --build)
        check_java
        check_maven
        print_status "Force rebuilding project..."
        cd "$PROJECT_DIR" || exit 1
        mvn clean package
        print_status "Build completed"
        exit 0
        ;;
    --clean)
        check_java
        check_maven
        print_status "Cleaning and rebuilding project..."
        cd "$PROJECT_DIR" || exit 1
        mvn clean package
        print_status "Clean build completed"
        exit 0
        ;;
    *)
        # Run normal application
        main "$@"
        ;;
esac