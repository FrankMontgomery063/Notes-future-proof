package com.notesapp.service;

import com.notesapp.model.Note;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages collections of notes with search and filtering capabilities.
 */
public class NoteRepository {
    
    private final NoteFileManager fileManager;
    private final Path notesDirectory;
    
    public NoteRepository(String notesDirectoryPath) {
        this.notesDirectory = Paths.get(notesDirectoryPath);
        this.fileManager = new NoteFileManager(notesDirectoryPath);
    }
    
    /**
     * Gets all note files in the directory.
     */
    private List<Path> getAllNoteFiles() throws IOException {
        if (!Files.exists(notesDirectory)) {
            return new ArrayList<>();
        }
        
        return Files.list(notesDirectory)
            .filter(path -> path.toString().endsWith(".note"))
            .collect(Collectors.toList());
    }
    
    /**
     * Loads all notes from the directory.
     */
    public List<Note> getAllNotes() throws IOException {
        List<Note> notes = new ArrayList<>();
        List<Path> noteFiles = getAllNoteFiles();
        
        for (Path file : noteFiles) {
            try {
                notes.add(fileManager.readNote(file));
            } catch (Exception e) {
                System.err.println("Warning: Could not read note file " + file + ": " + e.getMessage());
            }
        }
        
        return notes;
    }
    
    /**
     * Filters notes by tag.
     */
    public List<Note> getNotesWithTag(String tag) throws IOException {
        return getAllNotes().stream()
            .filter(note -> note.hasTag(tag))
            .collect(Collectors.toList());
    }
    
    /**
     * Finds a note by ID or title match.
     */
    public Note findNote(String identifier) throws IOException {
        List<Note> allNotes = getAllNotes();
        
        // Try exact ID match first
        for (Note note : allNotes) {
            if (identifier.equals(note.getId())) {
                return note;
            }
        }
        
        // Try partial title match
        for (Note note : allNotes) {
            if (note.getTitle().toLowerCase().contains(identifier.toLowerCase())) {
                return note;
            }
        }
        
        return null;
    }
    
    /**
     * Searches notes by content, title, or tags.
     */
    public List<Note> searchNotes(String query, boolean titleOnly, boolean contentOnly) throws IOException {
        String lowerQuery = query.toLowerCase();
        
        return getAllNotes().stream()
            .filter(note -> matchesSearchQuery(note, lowerQuery, titleOnly, contentOnly))
            .collect(Collectors.toList());
    }
    
    /**
     * Checks if note matches search criteria.
     */
    private boolean matchesSearchQuery(Note note, String query, boolean titleOnly, boolean contentOnly) {
        if (titleOnly) {
            return note.getTitle().toLowerCase().contains(query);
        }
        
        if (contentOnly) {
            return note.getContent().toLowerCase().contains(query);
        }
        
        // Search all fields
        return note.getTitle().toLowerCase().contains(query) ||
               note.getContent().toLowerCase().contains(query) ||
               note.getTags().stream().anyMatch(tag -> tag.toLowerCase().contains(query)) ||
               (note.getAuthor() != null && note.getAuthor().toLowerCase().contains(query));
    }
    
    /**
     * Saves a note using the file manager.
     */
    public void saveNote(Note note) throws IOException {
        fileManager.saveNote(note);
    }
    
    /**
     * Deletes a note file.
     */
    public boolean deleteNote(Note note) throws IOException {
        if (note.getFilePath() != null) {
            Path filePath = Paths.get(note.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                return true;
            }
        }
        return false;
    }
}