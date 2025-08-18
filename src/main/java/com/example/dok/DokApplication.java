package com.example.dok;

import com.example.dok.model.MarkdownFile;
import com.example.dok.repository.MarkdownFileRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DokApplication {

    public static void main(String[] args) {
        SpringApplication.run(DokApplication.class, args);
    }

    @Bean
    public CommandLineRunner initDatabase(MarkdownFileRepository repository) {
        return args -> {
            if (!repository.existsByPath("/")) {
                repository.save(new MarkdownFile("/", "/", true, null));
            }
        };
    }
}
