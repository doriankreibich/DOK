package com.example.dok.repository;

import com.example.dok.model.MarkdownFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface MarkdownFileRepository extends JpaRepository<MarkdownFile, Long> {

    Optional<MarkdownFile> findByPath(String path);

    List<MarkdownFile> findByPathStartingWith(String path);

    boolean existsByPath(String path);

    @Transactional
    void deleteByPath(String path);

    @Transactional
    void deleteByPathStartingWith(String path);

    @Query(value = "SELECT * FROM MARKDOWN_FILE WHERE REGEXP_LIKE(path, ?1)", nativeQuery = true)
    List<MarkdownFile> findByPathRegex(String regex);
}
