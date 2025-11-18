package com.notesapp.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Note Model Tests")
class NoteTest {
    
    private Note note;
    
    @BeforeEach
    void setUp() {
        note = new Note();
    }
    
    @Test
    @DisplayName("Should create note with default values")
    void shouldCreateNoteWithDefaults() {
        assertThat(note.getTags()).isEmpty();
        assertThat(note.getContent()).isEmpty();
        assertThat(note.getCreated()).isNotNull();
        assertThat(note.getModified()).isNotNull();
    }
    
    @Test
    @DisplayName("Should create note with title and content")
    void shouldCreateNoteWithTitleAndContent() {
        Note note = new Note("Test Title", "Test content");
        
        assertThat(note.getTitle()).isEqualTo("Test Title");
        assertThat(note.getContent()).isEqualTo("Test content");
        assertThat(note.isValid()).isTrue();
    }
    
    @Test
    @DisplayName("Should handle null content gracefully")
    void shouldHandleNullContentGracefully() {
        Note note = new Note("Test Title", null);
        
        assertThat(note.getContent()).isEmpty();
    }
    
    @Test
    @DisplayName("Should update modified time when title changes")
    void shouldUpdateModifiedTimeWhenTitleChanges() {
        LocalDateTime originalModified = note.getModified();
        
        // Wait a tiny bit to ensure time difference
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        note.setTitle("New Title");
        
        assertThat(note.getModified()).isAfter(originalModified);
    }
    
    @Test
    @DisplayName("Should manage tags correctly")
    void shouldManageTagsCorrectly() {
        note.addTag("java");
        note.addTag("programming");
        
        assertThat(note.getTags()).containsExactly("java", "programming");
        assertThat(note.hasTag("java")).isTrue();
        assertThat(note.hasTag("python")).isFalse();
    }
    
    @Test
    @DisplayName("Should not add duplicate tags")
    void shouldNotAddDuplicateTags() {
        note.addTag("java");
        note.addTag("java");
        
        assertThat(note.getTags()).containsExactly("java");
    }
    
    @Test
    @DisplayName("Should remove tags correctly")
    void shouldRemoveTagsCorrectly() {
        note.setTags(Arrays.asList("java", "programming", "tutorial"));
        note.removeTag("programming");
        
        assertThat(note.getTags()).containsExactly("java", "tutorial");
    }
    
    @Test
    @DisplayName("Should validate required fields")
    void shouldValidateRequiredFields() {
        // Empty note should not be valid
        assertThat(note.isValid()).isFalse();
        
        // Note with only title should be valid (created/modified auto-set)
        note.setTitle("Valid Title");
        assertThat(note.isValid()).isTrue();
        
        // Empty or null title should make note invalid
        note.setTitle("");
        assertThat(note.isValid()).isFalse();
        
        note.setTitle(null);
        assertThat(note.isValid()).isFalse();
    }
    
    @Test
    @DisplayName("Should generate consistent ID")
    void shouldGenerateConsistentId() {
        note.setTitle("My Test Note");
        String id1 = note.getId();
        String id2 = note.getId();
        
        assertThat(id1).isNotNull();
        assertThat(id1).isEqualTo(id2);
        assertThat(id1).startsWith("my-test-note-");
    }
    
    @Test
    @DisplayName("Should handle special characters in title for ID")
    void shouldHandleSpecialCharactersInTitleForId() {
        note.setTitle("My Note! @#$% With Special & Characters");
        String id = note.getId();
        
        assertThat(id).matches("^[a-z0-9\\-]+$");
        assertThat(id).startsWith("my-note-with-special-characters-");
    }
    
    @Test
    @DisplayName("Should format dates for YAML")
    void shouldFormatDatesForYaml() {
        note.setTitle("Test");
        
        String formattedCreated = note.getFormattedCreated();
        String formattedModified = note.getFormattedModified();
        
        assertThat(formattedCreated).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z");
        assertThat(formattedModified).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z");
    }
    
    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        Note note1 = new Note("Same Title", "Same content");
        Note note2 = new Note("Same Title", "Same content");
        Note note3 = new Note("Different Title", "Same content");
        
        // Set same creation time to make them equal
        LocalDateTime sameTime = LocalDateTime.now();
        note1.setCreated(sameTime);
        note2.setCreated(sameTime);
        
        assertThat(note1).isEqualTo(note2);
        assertThat(note1.hashCode()).isEqualTo(note2.hashCode());
        assertThat(note1).isNotEqualTo(note3);
    }
}