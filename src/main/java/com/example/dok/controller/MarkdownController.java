package com.example.dok.controller;

import com.example.dok.model.MarkdownFile;
import com.example.dok.repository.MarkdownFileRepository;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class MarkdownController {

    private final MarkdownFileRepository repository;
    private final Parser parser;
    private final HtmlRenderer renderer;

    public MarkdownController(MarkdownFileRepository repository, Parser parser, HtmlRenderer renderer) {
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

    public record FileEntry(String name, String path, boolean isDirectory) {}

    @GetMapping("/list")
    public List<FileEntry> listFiles(@RequestParam(defaultValue = "/") String path) {
        String parentPath = path.endsWith("/") ? path : path + "/";
        if (parentPath.equals("//")) parentPath = "/";

        String finalParentPath = parentPath;
        return repository.findByPathStartingWith(parentPath).stream()
                .filter(file -> !file.getPath().equals(finalParentPath))
                .filter(file -> {
                    String relativePath = file.getPath().substring(finalParentPath.length());
                    return !relativePath.contains("/");
                })
                .map(file -> new FileEntry(file.getName(), file.getPath(), file.isDirectory()))
                .collect(Collectors.toList());
    }

    @GetMapping("/view")
    public String viewMarkdown(@RequestParam String path) {
        String normalizedPath = normalizePath(path);
        return repository.findByPath(normalizedPath)
                .map(file -> {
                    if (file.isDirectory()) return "<p>Cannot render a directory.</p>";
                    Node document = parser.parse(file.getContent());
                    return renderer.render(document);
                })
                .orElse("<p style=\"color: red;\">Error: File not found in database.</p>");
    }

    @GetMapping("/raw")
    public String rawMarkdown(@RequestParam String path) {
        String normalizedPath = normalizePath(path);
        return repository.findByPath(normalizedPath)
                .map(MarkdownFile::getContent)
                .orElse("Error: File not found in database.");
    }

    @PostMapping("/save")
    public String saveMarkdown(@RequestParam String path, @RequestParam String content) {
        String normalizedPath = normalizePath(path);
        return repository.findByPath(normalizedPath).map(file -> {
            if (file.isDirectory()) {
                return "Error: Cannot save content to a directory.";
            }
            file.setContent(content);
            repository.save(file);
            return "File saved successfully!";
        }).orElse("Error: File not found.");
    }

    @PostMapping("/create-file")
    public String createFile(@RequestParam String path) {
        String normalizedPath = normalizePath(path);
        if (repository.existsByPath(normalizedPath)) {
            return "Error: File or directory with this name already exists.";
        }
        String name = normalizedPath.substring(normalizedPath.lastIndexOf('/') + 1);
        MarkdownFile newFile = new MarkdownFile(normalizedPath, name, false, "# New File\n");
        repository.save(newFile);
        return "File created successfully!";
    }

    @PostMapping("/create-directory")
    public String createDirectory(@RequestParam String path) {
        String normalizedPath = normalizePath(path);
        if (repository.existsByPath(normalizedPath)) {
            return "Error: File or directory with this name already exists.";
        }
        String name = normalizedPath.substring(normalizedPath.lastIndexOf('/') + 1);
        MarkdownFile newDir = new MarkdownFile(normalizedPath, name, true, null);
        repository.save(newDir);
        return "Directory created successfully!";
    }

    @PostMapping("/move")
    public String move(@RequestParam String source, @RequestParam String destination) {
        String normalizedSource = normalizePath(source);
        String normalizedDestination = normalizePath(destination);

        return repository.findByPath(normalizedSource).map(sourceFile -> {
            String newPath;
            if (normalizedDestination.equals("/")) {
                newPath = "/" + sourceFile.getName();
            } else {
                newPath = normalizedDestination + "/" + sourceFile.getName();
            }

            if (repository.existsByPath(newPath)) {
                return "Error: A file or directory with that name already exists in the destination.";
            }

            if (sourceFile.isDirectory()) {
                List<MarkdownFile> children = repository.findByPathStartingWith(normalizedSource + "/");
                for (MarkdownFile child : children) {
                    String childNewPath = child.getPath().replaceFirst(java.util.regex.Pattern.quote(normalizedSource), newPath);
                    child.setPath(childNewPath);
                    repository.save(child);
                }
            }
            sourceFile.setPath(newPath);
            repository.save(sourceFile);
            return "Moved successfully!";
        }).orElse("Error: Source file not found.");
    }

    @DeleteMapping("/delete")
    public String delete(@RequestParam String path) {
        String normalizedPath = normalizePath(path);
        if (normalizedPath.equals("/")) {
            return "Error: Cannot delete the root directory.";
        }
        return repository.findByPath(normalizedPath).map(file -> {
            if (file.isDirectory()) {
                repository.deleteByPathStartingWith(normalizedPath);
            } else {
                repository.deleteByPath(normalizedPath);
            }
            return "Deleted successfully!";
        }).orElse("Error: File or directory not found.");
    }
}
