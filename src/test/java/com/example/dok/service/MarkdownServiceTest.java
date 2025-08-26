package com.example.dok.service;

import com.example.dok.dto.FileEntryDto;
import com.example.dok.dto.MoveFileRequestDto;
import com.example.dok.dto.UpdateFileContentRequestDto;
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
        UpdateFileContentRequestDto request = new UpdateFileContentRequestDto(path, content);
        MarkdownFile file = MarkdownFile.builder()
                .path(path)
                .name("file.md")
                .isDirectory(false)
                .content("Old content")
                .build();

        when(repository.findByPath(path)).thenReturn(Optional.of(file));

        String result = markdownService.saveMarkdown(request);

        assertEquals("File saved successfully!", result);
        verify(repository).save(file);
        assertEquals(content, file.getContent());
    }

    @Test
    void delete_shouldDeleteFile_whenPathExists() {
        String path = "/docs/file-to-delete.md";
        MarkdownFile file = MarkdownFile.builder().path(path).name("file-to-delete.md").isDirectory(false).build();
        when(repository.findByPath(path)).thenReturn(Optional.of(file));

        String result = markdownService.delete(path);

        assertEquals("Deleted successfully!", result);
        verify(repository).deleteByPath(path);
    }

    @Test
    void delete_shouldDeleteDirectory_whenPathExists() {
        String path = "/docs/dir-to-delete";
        MarkdownFile dir = MarkdownFile.builder().path(path).name("dir-to-delete").isDirectory(true).build();
        when(repository.findByPath(path)).thenReturn(Optional.of(dir));

        String result = markdownService.delete(path);

        assertEquals("Deleted successfully!", result);
        verify(repository).deleteByPathStartingWith(path);
    }

    @Test
    void move_shouldMoveFile_whenDestinationIsEmpty() {
        String source = "/docs/source.md";
        String destination = "/new-docs";
        MoveFileRequestDto request = new MoveFileRequestDto(source, destination);
        MarkdownFile sourceFile = MarkdownFile.builder()
                .path(source)
                .name("source.md")
                .isDirectory(false)
                .content("content")
                .build();

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
        MarkdownFile childFile = MarkdownFile.builder().path(path + "/file.md").name("file.md").isDirectory(false).build();
        MarkdownFile childDir = MarkdownFile.builder().path(path + "/subdir").name("subdir").isDirectory(true).build();
        MarkdownFile grandchild = MarkdownFile.builder().path(path + "/subdir/grandchild.md").name("grandchild.md").isDirectory(false).build();

        when(repository.findByPathStartingWith(path + "/")).thenReturn(List.of(childFile, childDir, grandchild));

        List<FileEntryDto> result = markdownService.listFiles(path);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(f -> f.name().equals("file.md")));
        assertTrue(result.stream().anyMatch(f -> f.name().equals("subdir")));
    }
}
