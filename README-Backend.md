# Backend Technical Guide

## Overview

The backend for DOK is a modern Spring Boot application that serves as the brains of the operation. It handles all business logic, including file system management, database interaction, and Markdown-to-HTML conversion. It exposes a clean RESTful API that the JavaScript frontend consumes, following best practices for API design and data integrity.

---

## Project Structure

The backend source code is organized into the following key packages:

-   `com.example.dok.config`: Contains Spring `@Configuration` classes, such as `MarkdownConfig` for setting up the Markdown parser.
-   `com.example.dok.controller`: Holds the `MarkdownController`, which defines the REST API endpoints and handles incoming HTTP requests.
-   `com.example.dok.dto`: Contains Data Transfer Objects (DTOs) used for encapsulating request data. This makes the API robust and easy to extend.
-   `com.example.dok.model`: Defines the JPA entity (`MarkdownFile`) that maps to the database table.
-   `com.example.dok.repository`: Includes the Spring Data JPA repository (`MarkdownFileRepository`) for database operations.
-   `com.example.dok.service`: Contains the core business logic in the `MarkdownService` class.
-   `src/test`: Contains a suite of unit tests built with JUnit 5 and Mockito to ensure the service layer is reliable.

---

## Core Components & Design Patterns

-   **`MarkdownController`**: The entry point for all API requests. It is responsible for web-layer concerns and delegates all business logic to the `MarkdownService`.
-   **`MarkdownService`**: The heart of the backend. It orchestrates all business logic for file operations. It is marked as `@Transactional` to ensure data atomicity.
-   **`MarkdownFileRepository`**: A JPA repository that provides a clean abstraction layer over the H2 database.
-   **Records for DTOs**: Request bodies for complex operations (like moving or saving files) are encapsulated in immutable Java `record` classes for clarity and safety.
-   **Builder Pattern**: The `MarkdownFile` entity uses the Builder pattern (via Lombok's `@Builder`) for clean and readable object instantiation.

---

## API Endpoints

| Method   | Path                  | Request Body                             | Description                                                                        |
| :------- | :-------------------- | :--------------------------------------- | :--------------------------------------------------------------------------------- |
| `GET`    | `/list`               | *N/A* (Uses `@RequestParam`)             | Lists the files and directories directly under a given path.                       |
| `GET`    | `/view`               | *N/A* (Uses `@RequestParam`)             | Renders the Markdown content of a file to HTML.                                    |
| `GET`    | `/raw`                | *N/A* (Uses `@RequestParam`)             | Retrieves the raw Markdown content of a file.                                      |
| `POST`   | `/save`               | `UpdateFileContentRequest` (JSON)        | Saves or updates the content of a Markdown file.                                   |
| `POST`   | `/create-file`        | *N/A* (Uses `@RequestParam`)             | Creates a new, empty Markdown file at the specified path.                          |
| `POST`   | `/create-directory`   | *N/A* (Uses `@RequestParam`)             | Creates a new directory at the specified path.                                     |
| `POST`   | `/move`               | `MoveFileRequest` (JSON)                 | Moves a file or directory from a source to a destination.                          |
| `DELETE` | `/delete`             | *N/A* (Uses `@RequestParam`)             | Deletes a file or an entire directory (including its children).                    |

---

## Business Logic & Data Integrity

-   **Path Normalization**: A private `normalizePath` method ensures that all file paths are clean and consistent.
-   **Transactional Operations**: All methods that modify the database (`save`, `create`, `move`, `delete`) are annotated with `@Transactional`. This guarantees that if any part of an operation fails, the entire transaction is rolled back, preventing the database from being left in an inconsistent state.
-   **DTO-Based Requests**: For `POST` requests with multiple parameters, the backend uses DTOs (e.g., `MoveFileRequest`) and the `@RequestBody` annotation. This is a modern practice that makes the API cleaner and more maintainable than using numerous request parameters.

---

## Testing Strategy

The backend includes a suite of unit tests for the `MarkdownService`. 

-   **Frameworks**: Tests are written using **JUnit 5** and **Mockito**.
-   **Approach**: The tests mock the `MarkdownFileRepository` to isolate the service layer. This allows for focused testing of the business logic (e.g., verifying that the correct repository methods are called) without needing to connect to a live database.

---

## Key Dependencies

-   **Spring Boot Starter Web**: For building the RESTful API.
-   **Spring Boot Starter Data JPA**: To simplify database access.
-   **H2 Database**: An in-memory database perfect for development and testing.
-   **Flexmark**: A high-performance Java library for parsing Markdown to HTML.
-   **Lombok**: To reduce boilerplate code with annotations like `@Builder`, `@Getter`, and `@Setter`.
