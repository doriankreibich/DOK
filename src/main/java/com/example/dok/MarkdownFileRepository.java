package com.example.dok;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MarkdownFileRepository extends JpaRepository<MarkdownFile, Long> {

    Optional<MarkdownFile> findByPath(String path);

    List<MarkdownFile> findByPathStartingWith(String path);

    boolean existsByPath(String path);

    void deleteByPath(String path);

    @Query(value = "SELECT * FROM MARKDOWN_FILE WHERE REGEXP_LIKE(path, ?1)", nativeQuery = true)
    List<MarkdownFile> findByPathRegex(String regex);
}
