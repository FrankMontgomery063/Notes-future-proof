package com.notesapp.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a note with YAML front matter metadata and content.
 * Follows the specification from the README for note structure.
 */
public class Note {
    
    // Required fields
    private String title;
    private LocalDateTime created;
    private LocalDateTime modified;
    
    // Optional fields
    private List<String> tags;
    private String author;
    private String status;
    private Integer priority;
    
    // Note content (after YAML front matter)
    private String content;
    
    // File path where this note is stored
    private String filePath;
    
    // ISO 8601 date formatter for YAML serialization
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    
    /**
     * Default constructor for creating new notes
     */
    public Note() {
        this.created = LocalDateTime.now();
        this.modified = LocalDateTime.now();
        this.tags = new ArrayList<>();
    }
    
    /**
     * Constructor with required fields
     */
    public Note(String title, String content) {
        this();
        this.title = title;
        this.content = content != null ? content : "";
    }
    
    // Getters and Setters
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
        updateModified();
    }
    
    public LocalDateTime getCreated() {
        return created;
    }
    
    public void setCreated(LocalDateTime created) {
        this.created = created;
    }
    
    public LocalDateTime getModified() {
        return modified;
    }
    
    public void setModified(LocalDateTime modified) {
        this.modified = modified;
    }
    
    public List<String> getTags() {
        return tags != null ? tags : new ArrayList<>();
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
        updateModified();
    }
    
    public void addTag(String tag) {
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }
        if (!this.tags.contains(tag)) {
            this.tags.add(tag);
            updateModified();
        }
    }
    
    public void removeTag(String tag) {
        if (this.tags != null) {
            this.tags.remove(tag);
            updateModified();
        }
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
        updateModified();
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
        updateModified();
    }
    
    public Integer getPriority() {
        return priority;
    }
    
    public void setPriority(Integer priority) {
        this.priority = priority;
        updateModified();
    }
    
    public String getContent() {
        return content != null ? content : "";
    }
    
    public void setContent(String content) {
        this.content = content;
        updateModified();
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    /**
     * Updates the modified timestamp to now
     */
    private void updateModified() {
        this.modified = LocalDateTime.now();
    }
    
    /**
     * Gets the formatted creation date for YAML output
     */
    public String getFormattedCreated() {
        return created != null ? created.format(ISO_FORMATTER) : null;
    }
    
    /**
     * Gets the formatted modified date for YAML output
     */
    public String getFormattedModified() {
        return modified != null ? modified.format(ISO_FORMATTER) : null;
    }
    
    /**
     * Checks if this note has the specified tag
     */
    public boolean hasTag(String tag) {
        return tags != null && tags.contains(tag);
    }
    
    /**
     * Gets a unique identifier for this note (based on title and creation time)
     */
    public String getId() {
        if (title == null || created == null) {
            return null;
        }
        // Simple ID: lowercase title with spaces replaced by hyphens, plus creation timestamp
        String titlePart = title.toLowerCase()
            .replaceAll("[^a-z0-9\\s]", "")
            .replaceAll("\\s+", "-");
        String timePart = String.valueOf(created.toEpochSecond(java.time.ZoneOffset.UTC));
        return titlePart + "-" + timePart;
    }
    
    /**
     * Validates that the note has all required fields
     */
    public boolean isValid() {
        return title != null && !title.trim().isEmpty() 
            && created != null 
            && modified != null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Note note = (Note) o;
        return Objects.equals(title, note.title) &&
               Objects.equals(created, note.created) &&
               Objects.equals(content, note.content);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(title, created, content);
    }
    
    @Override
    public String toString() {
        return "Note{" +
                "title='" + title + '\'' +
                ", created=" + created +
                ", tags=" + tags +
                ", status='" + status + '\'' +
                '}';
    }
}