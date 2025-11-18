package com.notesapp.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Handles text editor integration for creating and editing notes.
 */
public class EditorService {
    
    /**
     * Opens a file in the system's default text editor.
     */
    public String editContent(String initialContent, String tempFileName) throws IOException, InterruptedException {
        Path tempFile = createTempFile(tempFileName, initialContent);
        
        try {
            openInEditor(tempFile);
            return Files.readString(tempFile);
        } finally {
            cleanupTempFile(tempFile);
        }
    }
    
    /**
     * Creates temporary file with initial content.
     */
    private Path createTempFile(String fileName, String content) throws IOException {
        Path tempFile = Paths.get(System.getProperty("java.io.tmpdir"), fileName + ".md");
        Files.write(tempFile, content.getBytes());
        return tempFile;
    }
    
    /**
     * Opens file in preferred text editor.
     */
    private void openInEditor(Path filePath) throws IOException, InterruptedException {
        String editor = getPreferredEditor();
        
        ProcessBuilder pb = new ProcessBuilder(editor, filePath.toString());
        pb.inheritIO(); // Allow user interaction with editor
        
        Process process = pb.start();
        int exitCode = process.waitFor();
        
        if (exitCode != 0) {
            throw new IOException("Editor exited with code " + exitCode);
        }
    }
    
    /**
     * Determines the best available text editor.
     */
    private String getPreferredEditor() {
        // Check environment variable first
        String editor = System.getenv("EDITOR");
        if (editor != null && !editor.trim().isEmpty()) {
            return editor;
        }
        
        // Try common editors in order of preference
        String[] editors = {"nano", "vim", "vi", "emacs", "pico"};
        
        for (String candidateEditor : editors) {
            if (isCommandAvailable(candidateEditor)) {
                return candidateEditor;
            }
        }
        
        // Fallback for different OS
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "notepad";
        } else if (os.contains("mac")) {
            return "open -e"; // TextEdit on macOS
        }
        
        return "vi"; // Universal fallback
    }
    
    /**
     * Checks if a command is available in the system PATH.
     */
    private boolean isCommandAvailable(String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder("which", command);
            Process process = pb.start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Removes temporary file.
     */
    private void cleanupTempFile(Path tempFile) {
        try {
            Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            System.err.println("Warning: Could not delete temp file " + tempFile);
        }
    }
}