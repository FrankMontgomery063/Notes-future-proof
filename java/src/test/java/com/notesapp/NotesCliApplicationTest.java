package com.notesapp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import picocli.CommandLine;

import java.io.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Notes CLI Application Tests")
class NotesCliApplicationTest {
    
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    
    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outputStream));
    }
    
    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }
    
    @Test
    @DisplayName("Should display help when no arguments provided")
    void shouldDisplayHelpWhenNoArgumentsProvided() {
        CommandLine cmd = new CommandLine(new NotesCliApplication());
        
        int exitCode = cmd.execute();
        String output = outputStream.toString();
        
        assertThat(exitCode).isEqualTo(0);
        assertThat(output).contains("Personal Notes Manager");
        assertThat(output).contains("Usage:");
    }
    
    @Test
    @DisplayName("Should show version information")
    void shouldShowVersionInformation() {
        CommandLine cmd = new CommandLine(new NotesCliApplication());
        
        int exitCode = cmd.execute("--version");
        String output = outputStream.toString();
        
        assertThat(exitCode).isEqualTo(0);
        assertThat(output).contains("1.0.0");
    }
    
    @Test
    @DisplayName("Should execute create command")
    void shouldExecuteCreateCommand() {
        CommandLine cmd = new CommandLine(new NotesCliApplication());
        
        int exitCode = cmd.execute("create", "--title", "Test Note");
        String output = outputStream.toString();
        
        assertThat(exitCode).isEqualTo(0);
        assertThat(output).contains("Creating new note");
        assertThat(output).contains("Title: Test Note");
    }
    
    @Test
    @DisplayName("Should execute list command")
    void shouldExecuteListCommand() {
        CommandLine cmd = new CommandLine(new NotesCliApplication());
        
        int exitCode = cmd.execute("list");
        String output = outputStream.toString();
        
        assertThat(exitCode).isEqualTo(0);
        assertThat(output).contains("Listing notes");
    }
    
    @Test
    @DisplayName("Should execute list command with tag filter")
    void shouldExecuteListCommandWithTagFilter() {
        CommandLine cmd = new CommandLine(new NotesCliApplication());
        
        int exitCode = cmd.execute("list", "--tag", "java");
        String output = outputStream.toString();
        
        assertThat(exitCode).isEqualTo(0);
        assertThat(output).contains("Listing notes");
        assertThat(output).contains("Filtering by tag: java");
    }
    
    @Test
    @DisplayName("Should execute search command")
    void shouldExecuteSearchCommand() {
        CommandLine cmd = new CommandLine(new NotesCliApplication());
        
        int exitCode = cmd.execute("search", "test query");
        String output = outputStream.toString();
        
        assertThat(exitCode).isEqualTo(0);
        assertThat(output).contains("Searching for: test query");
    }
    
    @Test
    @DisplayName("Should execute stats command")
    void shouldExecuteStatsCommand() {
        CommandLine cmd = new CommandLine(new NotesCliApplication());
        
        int exitCode = cmd.execute("stats");
        String output = outputStream.toString();
        
        assertThat(exitCode).isEqualTo(0);
        assertThat(output).contains("Notes statistics");
    }
}