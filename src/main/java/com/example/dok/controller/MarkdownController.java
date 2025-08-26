package com.example.dok.controller;

import com.example.dok.dto.FileEntryDto;
import com.example.dok.dto.MoveFileRequestDto;
import com.example.dok.dto.UpdateFileContentRequestDto;
import com.example.dok.service.MarkdownService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MarkdownController {

    private final MarkdownService markdownService;

    public MarkdownController(MarkdownService markdownService) {
        this.markdownService = markdownService;
    }

    @GetMapping("/list")
    public List<FileEntryDto> listFiles(@RequestParam(defaultValue = "/") String path) {
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
    public String saveMarkdown(@RequestBody UpdateFileContentRequestDto request) {
        return markdownService.saveMarkdown(request);
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
    public String move(@RequestBody MoveFileRequestDto request) {
        return markdownService.move(request);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> delete(@RequestParam String path) {
        try {
            markdownService.delete(path);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
