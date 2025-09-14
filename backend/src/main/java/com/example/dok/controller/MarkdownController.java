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
    public ResponseEntity<List<FileEntryDto>> listFiles(@RequestParam(defaultValue = "/") String path) {
        try {
            return ResponseEntity.ok(markdownService.listFiles(path));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/view")
    public ResponseEntity<String> viewMarkdown(@RequestParam String path) {
        try {
            return ResponseEntity.ok(markdownService.viewMarkdown(path));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/raw")
    public ResponseEntity<String> rawMarkdown(@RequestParam String path) {
        try {
            return ResponseEntity.ok(markdownService.rawMarkdown(path));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/save")
    public ResponseEntity<String> saveMarkdown(@RequestBody UpdateFileContentRequestDto request) {
        try {
            return ResponseEntity.ok(markdownService.saveMarkdown(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/create-file")
    public ResponseEntity<String> createFile(@RequestParam String path) {
        try {
            return ResponseEntity.ok(markdownService.createFile(path));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/create-directory")
    public ResponseEntity<String> createDirectory(@RequestParam String path) {
        try {
            return ResponseEntity.ok(markdownService.createDirectory(path));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/move")
    public ResponseEntity<String> move(@RequestBody MoveFileRequestDto request) {
        try {
            return ResponseEntity.ok(markdownService.move(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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
