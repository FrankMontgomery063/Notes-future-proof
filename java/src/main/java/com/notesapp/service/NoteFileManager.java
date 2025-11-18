package com.notesapp.service;

import com.notesapp.model.Note;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Handles file I/O operations for notes with YAML front matter.
 * Follows the README specification for note file format.
 */
public class NoteFileManager {
    
    private static final String YAML_DELIMITER = "---";
    private static final String FILE_EXTENSION = ".note";
    
    private final Path notesDirectory;
    private final Yaml yaml;
    
    public NoteFileManager(String notesDirectoryPath) {
        this.notesDirectory = Paths.get(notesDirectoryPath);
        this.yaml = new Yaml();
        ensureDirectoryExists();
    }
    
    /**
     * Ensures the notes directory exists, creates it if not.
     */
    private void ensureDirectoryExists() {
        try {
            if (!Files.exists(notesDirectory)) {
                Files.createDirectories(notesDirectory);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create notes directory: " + notesDirectory, e);
        }
    }
    
    /**
     * Generates a unique filename based on note title and timestamp.
     */
    private String generateFileName(String title) {
        String cleanTitle = title.toLowerCase()
            .replaceAll("[^a-z0-9\\s]", "")
            .replaceAll("\\s+", "-")
            .substring(0, Math.min(title.length(), 50));
        
        long timestamp = System.currentTimeMillis();
        return cleanTitle + "-" + timestamp + FILE_EXTENSION;
    }
    
    /**
     * Creates YAML header map from Note object.
     */
    private Map<String, Object> createYamlHeader(Note note) {
        Map<String, Object> header = new LinkedHashMap<>();
        header.put("title", note.getTitle());
        header.put("created", note.getFormattedCreated());
        header.put("modified", note.getFormattedModified());
        
        if (!note.getTags().isEmpty()) {
            header.put("tags", note.getTags());
        }
        if (note.getAuthor() != null) {
            header.put("author", note.getAuthor());
        }
        if (note.getStatus() != null) {
            header.put("status", note.getStatus());
        }
        if (note.getPriority() != null) {
            header.put("priority", note.getPriority());
        }
        
        return header;
    }
    
    /**
     * Formats complete note content with YAML header and content.
     */
    private String formatNoteContent(Note note) {
        StringBuilder content = new StringBuilder();
        content.append(YAML_DELIMITER).append("\n");
        content.append(yaml.dump(createYamlHeader(note)));
        content.append(YAML_DELIMITER).append("\n");
        if (note.getContent() != null && !note.getContent().trim().isEmpty()) {
            content.append("\n").append(note.getContent());
        }
        return content.toString();
    }
    
    /**
     * Saves a note to the file system.
     */
    public void saveNote(Note note) throws IOException {
        if (!note.isValid()) {
            throw new IllegalArgumentException("Note is not valid");
        }
        
        String fileName = generateFileName(note.getTitle());
        Path filePath = notesDirectory.resolve(fileName);
        String content = formatNoteContent(note);
        
        Files.write(filePath, content.getBytes());
        note.setFilePath(filePath.toString());
    }
    
    /**
     * Reads a note from file path.
     */
    public Note readNote(Path filePath) throws IOException {
        List<String> lines = Files.readAllLines(filePath);
        return parseNoteFromLines(lines, filePath);
    }
    
    /**
     * Parses note content from file lines.
     */
    private Note parseNoteFromLines(List<String> lines, Path filePath) {
        if (lines.isEmpty() || !YAML_DELIMITER.equals(lines.get(0))) {
            throw new IllegalArgumentException("Invalid note format - missing YAML delimiter");
        }
        
        int endYamlIndex = findEndYamlDelimiter(lines);
        Map<String, Object> yamlData = parseYamlHeader(lines, endYamlIndex);
        String content = extractContent(lines, endYamlIndex);
        
        return createNoteFromYaml(yamlData, content, filePath);
    }
    
    /**
     * Finds the end YAML delimiter index.
     */
    private int findEndYamlDelimiter(List<String> lines) {
        for (int i = 1; i < lines.size(); i++) {
            if (YAML_DELIMITER.equals(lines.get(i).trim())) {
                return i;
            }
        }
        throw new IllegalArgumentException("Invalid note format - missing end YAML delimiter");
    }
    
    /**
     * Parses YAML header from lines.
     */
    private Map<String, Object> parseYamlHeader(List<String> lines, int endIndex) {
        StringBuilder yamlContent = new StringBuilder();
        for (int i = 1; i < endIndex; i++) {
            yamlContent.append(lines.get(i)).append("\n");
        }
        return yaml.load(yamlContent.toString());
    }
    
    /**
     * Extracts content after YAML header.
     */
    private String extractContent(List<String> lines, int startIndex) {
        if (startIndex + 1 >= lines.size()) {
            return "";
        }
        
        StringBuilder content = new StringBuilder();
        for (int i = startIndex + 1; i < lines.size(); i++) {
            if (i > startIndex + 1) content.append("\n");
            content.append(lines.get(i));
        }
        return content.toString().trim();
    }
    
    /**
     * Creates Note object from parsed YAML data.
     */
    private Note createNoteFromYaml(Map<String, Object> yamlData, String content, Path filePath) {
        Note note = new Note();
        note.setTitle((String) yamlData.get("title"));
        note.setContent(content);
        note.setFilePath(filePath.toString());
        
        // Parse optional fields
        if (yamlData.containsKey("tags")) {
            Object tagsObj = yamlData.get("tags");
            if (tagsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> tags = (List<String>) tagsObj;
                note.setTags(tags);
            }
        }
        if (yamlData.containsKey("author")) {
            note.setAuthor((String) yamlData.get("author"));
        }
        if (yamlData.containsKey("status")) {
            note.setStatus((String) yamlData.get("status"));
        }
        if (yamlData.containsKey("priority")) {
            note.setPriority((Integer) yamlData.get("priority"));
        }
        
        return note;
    }
}