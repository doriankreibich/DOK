package com.example.dok.controller;

import com.example.dok.dto.FileEntry;
import com.example.dok.service.MarkdownService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MarkdownController {

    private final MarkdownService markdownService;

    public MarkdownController(MarkdownService markdownService) {
        this.markdownService = markdownService;
    }

    @GetMapping("/list")
    public List<FileEntry> listFiles(@RequestParam(defaultValue = "/") String path) {
        return markdownService.listFiles(path);
    }

    @GetMapping("/view")
    public String viewMarkdown(@RequestParam String path) {
        return markdownService.viewMarkdown(path);
    }

    @GetMapping("/raw")
    public String rawMarkdown(@RequestParam String path) {
        return markdownService.rawMarkdown(path);
    }

    @PostMapping("/save")
    public String saveMarkdown(@RequestParam String path, @RequestParam String content) {
        return markdownService.saveMarkdown(path, content);
    }

    @PostMapping("/create-file")
    public String createFile(@RequestParam String path) {
        return markdownService.createFile(path);
    }

    @PostMapping("/create-directory")
    public String createDirectory(@RequestParam String path) {
        return markdownService.createDirectory(path);
    }

    @PostMapping("/move")
    public String move(@RequestParam String source, @RequestParam String destination) {
        return markdownService.move(source, destination);
    }

    @DeleteMapping("/delete")
    public String delete(@RequestParam String path) {
        return markdownService.delete(path);
    }
}
