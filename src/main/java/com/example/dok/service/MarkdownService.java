package com.example.dok.service;

import com.example.dok.dto.FileEntryDto;
import com.example.dok.dto.MoveFileRequestDto;
import com.example.dok.dto.UpdateFileContentRequestDto;
import com.example.dok.model.MarkdownFile;
import com.example.dok.repository.MarkdownFileRepository;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MarkdownService {

    private final MarkdownFileRepository repository;
    private final Parser parser;
    private final HtmlRenderer renderer;

    public MarkdownService(MarkdownFileRepository repository, Parser parser, HtmlRenderer renderer) {
        this.repository = repository;
        this.parser = parser;
        this.renderer = renderer;
    }

    private String normalizePath(String path) {
        if (path == null) return "/";
        String cleanedPath = path.replaceAll("//+", "/");
        if (cleanedPath.length() > 1 && cleanedPath.endsWith("/")) {
            cleanedPath = cleanedPath.substring(0, cleanedPath.length() - 1);
        }
        return cleanedPath;
    }

    private boolean isDirectChild(MarkdownFile file, String parentPath) {
        if (file.getPath().equals(parentPath)) {
            return false;
        }
        String relativePath = file.getPath().substring(parentPath.length());
        return !relativePath.contains("/");
    }

    public List<FileEntryDto> listFiles(String path) {
        String parentPath = path.endsWith("/") ? path : path + "/";
        if (parentPath.equals("//")) parentPath = "/";

        String finalParentPath = parentPath;
        return repository.findByPathStartingWith(parentPath).stream()
                .filter(file -> isDirectChild(file, finalParentPath))
                .map(file -> new FileEntryDto(file.getName(), file.getPath(), file.isDirectory()))
                .collect(Collectors.toList());
    }

    public String viewMarkdown(String path) {
        String normalizedPath = normalizePath(path);
        return repository.findByPath(normalizedPath)
                .map(file -> {
                    if (file.isDirectory()) return "<p>Cannot render a directory.</p>";
                    Node document = parser.parse(file.getContent());
                    return renderer.render(document);
                })
                .orElse("<p style=\"color: red;\">Error: File not found in database.</p>");
    }

    public String rawMarkdown(String path) {
        String normalizedPath = normalizePath(path);
        return repository.findByPath(normalizedPath)
                .map(MarkdownFile::getContent)
                .orElse("Error: File not found in database.");
    }

    @Transactional
    public String saveMarkdown(UpdateFileContentRequestDto request) {
        String normalizedPath = normalizePath(request.path());
        Optional<MarkdownFile> fileOptional = repository.findByPath(normalizedPath);

        if (fileOptional.isEmpty()) {
            return "Error: File not found.";
        }

        MarkdownFile file = fileOptional.get();
        if (file.isDirectory()) {
            return "Error: Cannot save content to a directory.";
        }

        file.setContent(request.content());
        repository.save(file);
        return "File saved successfully!";
    }

    @Transactional
    public String createFile(String path) {
        String normalizedPath = normalizePath(path);
        if (repository.existsByPath(normalizedPath)) {
            return "Error: File or directory with this name already exists.";
        }
        String name = normalizedPath.substring(normalizedPath.lastIndexOf('/') + 1);
        MarkdownFile newFile = MarkdownFile.builder()
                .path(normalizedPath)
                .name(name)
                .isDirectory(false)
                .content("# New File\n")
                .build();
        repository.save(newFile);
        return "File created successfully!";
    }

    @Transactional
    public String createDirectory(String path) {
        String normalizedPath = normalizePath(path);
        if (repository.existsByPath(normalizedPath)) {
            return "Error: File or directory with this name already exists.";
        }
        String name = normalizedPath.substring(normalizedPath.lastIndexOf('/') + 1);
        MarkdownFile newDir = MarkdownFile.builder()
                .path(normalizedPath)
                .name(name)
                .isDirectory(true)
                .build();
        repository.save(newDir);
        return "Directory created successfully!";
    }

    @Transactional
    public String move(MoveFileRequestDto request) {
        String normalizedSource = normalizePath(request.source());
        String normalizedDestination = normalizePath(request.destination());

        Optional<MarkdownFile> sourceFileOpt = repository.findByPath(normalizedSource);
        if (sourceFileOpt.isEmpty()) {
            return "Error: Source file not found.";
        }
        MarkdownFile sourceFile = sourceFileOpt.get();

        String newPath = normalizedDestination.equals("/")
                ? "/" + sourceFile.getName()
                : normalizedDestination + "/" + sourceFile.getName();

        if (repository.existsByPath(newPath)) {
            return "Error: A file or directory with that name already exists in the destination.";
        }

        if (sourceFile.isDirectory()) {
            List<MarkdownFile> children = repository.findByPathStartingWith(normalizedSource + "/");
            for (MarkdownFile child : children) {
                String childNewPath = child.getPath().replaceFirst(java.util.regex.Pattern.quote(normalizedSource), newPath);
                child.setPath(childNewPath);
            }
            repository.saveAll(children);
        }

        sourceFile.setPath(newPath);
        repository.save(sourceFile);
        return "Moved successfully!";
    }

    @Transactional
    public String delete(String path) {
        String normalizedPath = normalizePath(path);
        if (normalizedPath.equals("/")) {
            return "Error: Cannot delete the root directory.";
        }

        Optional<MarkdownFile> fileOptional = repository.findByPath(normalizedPath);
        if (fileOptional.isEmpty()) {
            return "Error: File or directory not found.";
        }

        MarkdownFile file = fileOptional.get();
        if (file.isDirectory()) {
            repository.deleteByPathStartingWith(normalizedPath);
        } else {
            repository.deleteByPath(normalizedPath);
        }
        return "Deleted successfully!";
    }
}
