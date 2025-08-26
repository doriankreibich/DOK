package com.example.dok.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class MarkdownFile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String path;

    private String name;

    private boolean isDirectory;

    @Lob
    @Column(length = 1000000)
    private String content;

    public MarkdownFile() {
    }

    public MarkdownFile(String path, String name, boolean isDirectory, String content) {
        this.path = path;
        this.name = name;
        this.isDirectory = isDirectory;
        this.content = content;
    }
}
