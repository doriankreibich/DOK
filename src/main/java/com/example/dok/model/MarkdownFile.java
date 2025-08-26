package com.example.dok.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}
