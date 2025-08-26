package com.example.dok.service;

import com.example.dok.dto.FileEntry;
import com.example.dok.dto.MoveFileRequest;
import com.example.dok.dto.UpdateFileContentRequest;
import com.example.dok.model.MarkdownFile;
import com.example.dok.repository.MarkdownFileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarkdownServiceTest {

    @Mock
    private MarkdownFileRepository repository;

    @InjectMocks
    private MarkdownService markdownService;

    @Test
    void createFile_shouldCreateFile_whenPathIsNew() {
        String path = "/docs/new-file.md";
        when(repository.existsByPath(path)).thenReturn(false);

        String result = markdownService.createFile(path);

        assertEquals("File created successfully!", result);
        verify(repository).save(any(MarkdownFile.class));
    }

    @Test
    void createFile_shouldReturnError_whenPathExists() {
        String path = "/docs/existing-file.md";
        when(repository.existsByPath(path)).thenReturn(true);

        String result = markdownService.createFile(path);

        assertEquals("Error: File or directory with this name already exists.", result);
        verify(repository, never()).save(any());
    }

    @Test
    void saveMarkdown_shouldUpdateFile() {
        String path = "/docs/file.md";
        String content = "New content";
        UpdateFileContentRequest request = new UpdateFileContentRequest(path, content);
        MarkdownFile file = new MarkdownFile(path, "file.md", false, "Old content");

        when(repository.findByPath(path)).thenReturn(Optional.of(file));

        String result = markdownService.saveMarkdown(request);

        assertEquals("File saved successfully!", result);
        verify(repository).save(file);
        assertEquals(content, file.getContent());
    }

    @Test
    void delete_shouldDeleteFile_whenPathExists() {
        String path = "/docs/file-to-delete.md";
        MarkdownFile file = new MarkdownFile(path, "file-to-delete.md", false, "");
        when(repository.findByPath(path)).thenReturn(Optional.of(file));

        String result = markdownService.delete(path);

        assertEquals("Deleted successfully!", result);
        verify(repository).deleteByPath(path);
    }

    @Test
    void delete_shouldDeleteDirectory_whenPathExists() {
        String path = "/docs/dir-to-delete";
        MarkdownFile dir = new MarkdownFile(path, "dir-to-delete", true, null);
        when(repository.findByPath(path)).thenReturn(Optional.of(dir));

        String result = markdownService.delete(path);

        assertEquals("Deleted successfully!", result);
        verify(repository).deleteByPathStartingWith(path);
    }

    @Test
    void move_shouldMoveFile_whenDestinationIsEmpty() {
        String source = "/docs/source.md";
        String destination = "/new-docs";
        MoveFileRequest request = new MoveFileRequest(source, destination);
        MarkdownFile sourceFile = new MarkdownFile(source, "source.md", false, "content");

        when(repository.findByPath(source)).thenReturn(Optional.of(sourceFile));
        when(repository.existsByPath(destination + "/" + sourceFile.getName())).thenReturn(false);

        String result = markdownService.move(request);

        assertEquals("Moved successfully!", result);
        verify(repository).save(sourceFile);
        assertEquals("/new-docs/source.md", sourceFile.getPath());
    }

    @Test
    void listFiles_shouldReturnDirectChildren() {
        String path = "/docs";
        MarkdownFile childFile = new MarkdownFile(path + "/file.md", "file.md", false, "");
        MarkdownFile childDir = new MarkdownFile(path + "/subdir", "subdir", true, null);
        MarkdownFile grandchild = new MarkdownFile(path + "/subdir/grandchild.md", "grandchild.md", false, "");

        when(repository.findByPathStartingWith(path + "/")).thenReturn(List.of(childFile, childDir, grandchild));

        List<FileEntry> result = markdownService.listFiles(path);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(f -> f.name().equals("file.md")));
        assertTrue(result.stream().anyMatch(f -> f.name().equals("subdir")));
    }
}
