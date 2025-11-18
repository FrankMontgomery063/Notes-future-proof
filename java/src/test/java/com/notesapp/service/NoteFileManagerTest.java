package com.notesapp.service;

import com.notesapp.model.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

@DisplayName("NoteFileManager Tests")
class NoteFileManagerTest {
    
    @TempDir
    Path tempDir;
    
    private NoteFileManager fileManager;
    
    @BeforeEach
    void setUp() {
        fileManager = new NoteFileManager(tempDir.toString());
    }
    
    @Test
    @DisplayName("Should save and read a simple note")
    void shouldSaveAndReadSimpleNote() throws IOException {
        // Create a note
        Note originalNote = new Note("Test Note", "This is test content");
        originalNote.setAuthor("TestUser");
        originalNote.addTag("test");
        originalNote.addTag("example");
        
        // Save it
        fileManager.saveNote(originalNote);
        
        // Read it back
        Path filePath = Path.of(originalNote.getFilePath());
        Note loadedNote = fileManager.readNote(filePath);
        
        // Verify
        assertThat(loadedNote.getTitle()).isEqualTo("Test Note");
        assertThat(loadedNote.getContent()).isEqualTo("This is test content");
        assertThat(loadedNote.getAuthor()).isEqualTo("TestUser");
        assertThat(loadedNote.getTags()).contains("test", "example");
    }
    
    @Test
    @DisplayName("Should handle note with all metadata fields")
    void shouldHandleNoteWithAllMetadata() throws IOException {
        // Create note with all fields
        Note note = new Note("Full Metadata Note", "Content here");
        note.setAuthor("Frank");
        note.setStatus("draft");
        note.setPriority(3);
        note.setTags(Arrays.asList("java", "programming", "notes"));
        
        // Save and reload
        fileManager.saveNote(note);
        Note loaded = fileManager.readNote(Path.of(note.getFilePath()));
        
        // Verify all fields
        assertThat(loaded.getTitle()).isEqualTo("Full Metadata Note");
        assertThat(loaded.getAuthor()).isEqualTo("Frank");
        assertThat(loaded.getStatus()).isEqualTo("draft");
        assertThat(loaded.getPriority()).isEqualTo(3);
        assertThat(loaded.getTags()).containsExactly("java", "programming", "notes");
    }
}