# Personal Notes Manager - Java CLI Implementation

A command-line interface for managing personal notes with YAML metadata, built in Java.

## Prerequisites

- Java 11 or higher
- Maven 3.6 or higher

## Quick Start

1. **Build the project:**
   ```bash
   mvn clean compile
   ```

2. **Run tests:**
   ```bash
   mvn test
   ```

3. **Build executable JAR:**
   ```bash
   mvn clean package
   ```

4. **Run the application:**
   ```bash
   ./run.sh --help
   ```

## Available Commands

### Create a new note
```bash
./run.sh create --title "My New Note" --tag java,programming
```

### List all notes
```bash
./run.sh list
```

### List notes with specific tag
```bash
./run.sh list --tag coursework
```

### Read a specific note
```bash
./run.sh read "note-id-or-title"
```

### Edit a note
```bash
./run.sh edit "note-id-or-title"
```

### Delete a note
```bash
./run.sh delete "note-id-or-title"
./run.sh delete "note-id-or-title" --force  # Skip confirmation
```

### Search notes
```bash
./run.sh search "search query"
./run.sh search "java" --content    # Search content only
./run.sh search "tutorial" --title  # Search titles only
```

### View statistics
```bash
./run.sh stats
```

## Project Structure

```
java/
├── pom.xml                     # Maven configuration
├── run.sh                      # Executable script
├── notes/                      # Default notes directory
├── src/
│   ├── main/java/com/notesapp/
│   │   ├── NotesCliApplication.java    # Main CLI application
│   │   └── model/
│   │       └── Note.java               # Note data model
│   └── test/java/com/notesapp/
│       ├── NotesCliApplicationTest.java
│       └── model/
│           └── NoteTest.java
└── target/                     # Build output (created by Maven)
```

## Note File Format

Notes are stored as text files with YAML front matter:

```markdown
---
title: My Example Note
created: 2025-11-17T10:30:00Z
modified: 2025-11-17T10:45:00Z
tags: [java, programming]
author: YourName
status: draft
priority: 2
---

This is the content of my note.

You can use **markdown** formatting here.

- Lists work too
- Second item
```

## Development

### Running Tests
```bash
mvn test
```

### Running Tests with Coverage
```bash
mvn test jacoco:report
```

### Building
```bash
mvn clean compile      # Compile only
mvn clean package      # Compile and create JAR
mvn clean install      # Install to local Maven repository
```

### IDE Setup
Import the project as a Maven project in your IDE. The main class is:
```
com.notesapp.NotesCliApplication
```

## Future Phases

This CLI implementation is Phase 1 of a three-phase project:
- **Phase 1**: Command Line Interface (current)
- **Phase 2**: Graphical User Interface
- **Phase 3**: REST Server with JavaScript Frontend

The file format and core data structures are designed to remain compatible across all phases.

## Contributing

1. Write tests first (TDD approach)
2. Keep the Note file format backward compatible
3. Follow Java naming conventions
4. Add JavaDoc for public methods
5. Run tests before committing

## Troubleshooting

**Build fails**: Ensure you have Java 11+ and Maven 3.6+ installed:
```bash
java -version
mvn -version
```

**Permission denied on run.sh**: Make the script executable:
```bash
chmod +x run.sh
```

**Notes not found**: The application looks for notes in the `notes/` directory by default.
