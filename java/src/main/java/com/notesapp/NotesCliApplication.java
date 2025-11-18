package com.notesapp;

import com.notesapp.model.Note;
import com.notesapp.service.EditorService;
import com.notesapp.service.NoteRepository;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;

@Command(
    name = "notes",
    description = "Personal Notes Manager - A CLI tool for managing notes with YAML metadata",
    version = "1.0.0",
    mixinStandardHelpOptions = true,
    subcommands = {
        NotesCliApplication.CreateCommand.class,
        NotesCliApplication.ListCommand.class,
        NotesCliApplication.ReadCommand.class,
        NotesCliApplication.EditCommand.class,
        NotesCliApplication.DeleteCommand.class,
        NotesCliApplication.SearchCommand.class,
        NotesCliApplication.StatsCommand.class
    }
)
public class NotesCliApplication implements Callable<Integer> {
    
    private static final String NOTES_DIR = "notes";
    
    public static void main(String[] args) {
        int exitCode = new CommandLine(new NotesCliApplication()).execute(args);
        System.exit(exitCode);
    }
    
    @Override
    public Integer call() throws Exception {
        // When no subcommand is provided, show help
        CommandLine.usage(this, System.out);
        return 0;
    }
    
    @Command(name = "create", description = "Create a new note")
    static class CreateCommand implements Callable<Integer> {
        
        @Option(names = {"-t", "--title"}, description = "Note title")
        private String title;
        
        @Option(names = {"--tag"}, description = "Add tags to the note", split = ",")
        private String[] tags;
        
        @Override
        public Integer call() throws Exception {
            try {
                // Get title if not provided
                if (title == null || title.trim().isEmpty()) {
                    title = promptForTitle();
                }
                
                // Create note with basic metadata
                Note note = new Note(title.trim(), "");
                
                // Add tags if provided
                if (tags != null) {
                    for (String tag : tags) {
                        note.addTag(tag.trim());
                    }
                }
                
                // Open in editor for content
                EditorService editor = new EditorService();
                String initialContent = "# " + title + "\n\nWrite your note content here...\n";
                String content = editor.editContent(initialContent, "note-" + System.currentTimeMillis());
                
                // Remove the initial template if unchanged
                if (!content.equals(initialContent)) {
                    note.setContent(content);
                }
                
                // Save the note
                NoteRepository repository = new NoteRepository(NOTES_DIR);
                repository.saveNote(note);
                
                System.out.println("Note created successfully: " + note.getTitle());
                System.out.println("ID: " + note.getId());
                
                return 0;
                
            } catch (Exception e) {
                System.err.println("Error creating note: " + e.getMessage());
                return 1;
            }
        }
        
        private String promptForTitle() {
            try (Scanner scanner = new Scanner(System.in)) {
                System.out.print("Enter note title: ");
                String input = scanner.nextLine();
                
                if (input.trim().isEmpty()) {
                    return "Untitled Note";
                }
                
                return input.trim();
            }
        }
    }
    
    @Command(name = "list", description = "List all notes or filter by tags")
    static class ListCommand implements Callable<Integer> {
        
        @Option(names = {"--tag"}, description = "Filter notes by tag")
        private String tag;
        
        @Override
        public Integer call() throws Exception {
            NoteRepository repository = new NoteRepository(NOTES_DIR);
            
            try {
                List<Note> notes = (tag != null) ? 
                    repository.getNotesWithTag(tag) : 
                    repository.getAllNotes();
                
                if (notes.isEmpty()) {
                    System.out.println(tag != null ? 
                        "No notes found with tag: " + tag : 
                        "No notes found. Create your first note with 'notes create'");
                    return 0;
                }
                
                displayNotesList(notes, tag);
                return 0;
                
            } catch (IOException e) {
                System.err.println("Error reading notes: " + e.getMessage());
                return 1;
            }
        }
        
        private void displayNotesList(List<Note> notes, String filterTag) {
            System.out.println(filterTag != null ? 
                "Notes with tag '" + filterTag + "':" : 
                "All notes:");
            System.out.println("─".repeat(50));
            
            for (Note note : notes) {
                System.out.printf("%-30s %s%n", 
                    truncate(note.getTitle(), 28),
                    formatTags(note.getTags()));
            }
            
            System.out.println("─".repeat(50));
            System.out.println("Total: " + notes.size() + " notes");
        }
        
        private String truncate(String text, int maxLength) {
            return text.length() > maxLength ? 
                text.substring(0, maxLength - 3) + "..." : text;
        }
        
        private String formatTags(List<String> tags) {
            return tags.isEmpty() ? "" : "[" + String.join(", ", tags) + "]";
        }
    }
    
    @Command(name = "read", description = "Display a specific note")
    static class ReadCommand implements Callable<Integer> {
        
        @Parameters(index = "0", description = "Note ID or title to read")
        private String noteId;
        
        @Override
        public Integer call() throws Exception {
            try {
                NoteRepository repository = new NoteRepository(NOTES_DIR);
                Note note = repository.findNote(noteId);
                
                if (note == null) {
                    System.err.println("Note not found: " + noteId);
                    return 1;
                }
                
                displayNote(note);
                return 0;
                
            } catch (IOException e) {
                System.err.println("Error reading note: " + e.getMessage());
                return 1;
            }
        }
        
        private void displayNote(Note note) {
            System.out.println("═".repeat(60));
            System.out.println("Title: " + note.getTitle());
            System.out.println("Created: " + note.getFormattedCreated());
            System.out.println("Modified: " + note.getFormattedModified());
            
            if (!note.getTags().isEmpty()) {
                System.out.println("Tags: " + String.join(", ", note.getTags()));
            }
            if (note.getAuthor() != null) {
                System.out.println("Author: " + note.getAuthor());
            }
            if (note.getStatus() != null) {
                System.out.println("Status: " + note.getStatus());
            }
            if (note.getPriority() != null) {
                System.out.println("Priority: " + note.getPriority());
            }
            
            System.out.println("═".repeat(60));
            System.out.println();
            System.out.println(note.getContent());
            System.out.println();
        }
    }
    
    @Command(name = "edit", description = "Edit a specific note")
    static class EditCommand implements Callable<Integer> {
        
        @Parameters(index = "0", description = "Note ID or title to edit")
        private String noteId;
        
        @Override
        public Integer call() throws Exception {
            try {
                NoteRepository repository = new NoteRepository(NOTES_DIR);
                Note note = repository.findNote(noteId);
                
                if (note == null) {
                    System.err.println("Note not found: " + noteId);
                    return 1;
                }
                
                // Open current content in editor
                EditorService editor = new EditorService();
                String currentContent = note.getContent().isEmpty() ? 
                    "# " + note.getTitle() + "\n\n" : note.getContent();
                
                String newContent = editor.editContent(currentContent, "edit-" + note.getId());
                
                // Update note if content changed
                if (!newContent.equals(currentContent)) {
                    note.setContent(newContent);
                    
                    // Delete old file and save with new timestamp
                    repository.deleteNote(note);
                    repository.saveNote(note);
                    
                    System.out.println("Note updated: " + note.getTitle());
                } else {
                    System.out.println("No changes made to: " + note.getTitle());
                }
                
                return 0;
                
            } catch (Exception e) {
                System.err.println("Error editing note: " + e.getMessage());
                return 1;
            }
        }
    }
    
    @Command(name = "delete", description = "Delete a specific note")
    static class DeleteCommand implements Callable<Integer> {
        
        @Parameters(index = "0", description = "Note ID or title to delete")
        private String noteId;
        
        @Option(names = {"-f", "--force"}, description = "Force delete without confirmation")
        private boolean force;
        
        @Override
        public Integer call() throws Exception {
            try {
                NoteRepository repository = new NoteRepository(NOTES_DIR);
                Note note = repository.findNote(noteId);
                
                if (note == null) {
                    System.err.println("Note not found: " + noteId);
                    return 1;
                }
                
                // Confirm deletion unless force flag is used
                if (!force && !confirmDeletion(note)) {
                    System.out.println("Deletion cancelled");
                    return 0;
                }
                
                if (repository.deleteNote(note)) {
                    System.out.println("Note deleted: " + note.getTitle());
                } else {
                    System.err.println("Failed to delete note file");
                    return 1;
                }
                
                return 0;
                
            } catch (IOException e) {
                System.err.println("Error deleting note: " + e.getMessage());
                return 1;
            }
        }
        
        private boolean confirmDeletion(Note note) {
            try (Scanner scanner = new Scanner(System.in)) {
                System.out.println("About to delete note: " + note.getTitle());
                System.out.print("Are you sure? (y/N): ");
                String response = scanner.nextLine().trim().toLowerCase();
                return response.equals("y") || response.equals("yes");
            }
        }
    }
    
    @Command(name = "search", description = "Search notes by content, title, or tags")
    static class SearchCommand implements Callable<Integer> {
        
        @Parameters(index = "0", description = "Search query")
        private String query;
        
        @Option(names = {"-c", "--content"}, description = "Search in content only")
        private boolean contentOnly;
        
        @Option(names = {"-t", "--title"}, description = "Search in titles only")
        private boolean titleOnly;
        
        @Override
        public Integer call() throws Exception {
            try {
                NoteRepository repository = new NoteRepository(NOTES_DIR);
                List<Note> results = repository.searchNotes(query, titleOnly, contentOnly);
                
                if (results.isEmpty()) {
                    System.out.println("No notes found matching: " + query);
                    return 0;
                }
                
                displaySearchResults(results, query);
                return 0;
                
            } catch (IOException e) {
                System.err.println("Error searching notes: " + e.getMessage());
                return 1;
            }
        }
        
        private void displaySearchResults(List<Note> results, String query) {
            System.out.println("Search results for '" + query + "':");
            System.out.println("─".repeat(50));
            
            for (Note note : results) {
                System.out.printf("%-30s %s%n", 
                    truncate(note.getTitle(), 28),
                    formatTags(note.getTags()));
                
                // Show a snippet of content if it matches
                String snippet = getContentSnippet(note.getContent(), query);
                if (!snippet.isEmpty()) {
                    System.out.println("  " + snippet);
                }
                System.out.println();
            }
            
            System.out.println("─".repeat(50));
            System.out.println("Found " + results.size() + " notes");
        }
        
        private String truncate(String text, int maxLength) {
            return text.length() > maxLength ? 
                text.substring(0, maxLength - 3) + "..." : text;
        }
        
        private String formatTags(List<String> tags) {
            return tags.isEmpty() ? "" : "[" + String.join(", ", tags) + "]";
        }
        
        private String getContentSnippet(String content, String query) {
            if (content == null || content.isEmpty()) return "";
            
            String lowerContent = content.toLowerCase();
            String lowerQuery = query.toLowerCase();
            int index = lowerContent.indexOf(lowerQuery);
            
            if (index == -1) return "";
            
            int start = Math.max(0, index - 30);
            int end = Math.min(content.length(), index + query.length() + 30);
            
            String snippet = content.substring(start, end).trim();
            return "..." + snippet + "...";
        }
    }
    
    @Command(name = "stats", description = "Display statistics about your notes")
    static class StatsCommand implements Callable<Integer> {
        
        @Override
        public Integer call() throws Exception {
            try {
                NoteRepository repository = new NoteRepository(NOTES_DIR);
                List<Note> notes = repository.getAllNotes();
                
                if (notes.isEmpty()) {
                    System.out.println("No notes found. Create your first note with 'notes create'");
                    return 0;
                }
                
                displayStatistics(notes);
                return 0;
                
            } catch (IOException e) {
                System.err.println("Error reading notes: " + e.getMessage());
                return 1;
            }
        }
        
        private void displayStatistics(List<Note> notes) {
            System.out.println("Notes Statistics");
            System.out.println("═".repeat(40));
            
            // Basic counts
            System.out.println("Total notes: " + notes.size());
            
            // Tag statistics
            List<String> allTags = notes.stream()
                .flatMap(note -> note.getTags().stream())
                .distinct()
                .sorted()
                .collect(java.util.stream.Collectors.toList());
            
            System.out.println("Unique tags: " + allTags.size());
            if (!allTags.isEmpty()) {
                System.out.println("Tags: " + String.join(", ", allTags));
            }
            
            // Content statistics
            int totalWords = notes.stream()
                .mapToInt(note -> countWords(note.getContent()))
                .sum();
            
            System.out.println("Total words: " + totalWords);
            System.out.println("Average words per note: " + 
                (notes.size() > 0 ? totalWords / notes.size() : 0));
            
            // Notes with authors
            long notesWithAuthors = notes.stream()
                .filter(note -> note.getAuthor() != null && !note.getAuthor().trim().isEmpty())
                .count();
            
            if (notesWithAuthors > 0) {
                System.out.println("Notes with authors: " + notesWithAuthors);
            }
        }
        
        private int countWords(String content) {
            if (content == null || content.trim().isEmpty()) {
                return 0;
            }
            
            return content.trim().split("\\s+").length;
        }
    }
}