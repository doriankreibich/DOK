# Backend Technical Guide

## Overview

The backend for DOK is a modern Spring Boot application that serves as the brains of the operation. It handles all business logic, including file system management, database interaction, and Markdown-to-HTML conversion. It exposes a clean RESTful API that the JavaScript frontend consumes, following best practices for API design and data integrity.

---

## Project Structure

-   `com.example.dok.config`: Spring `@Configuration` classes.
-   `com.example.dok.controller`: The `MarkdownController`, which defines the REST API endpoints.
-   `com.example.dok.dto`: Data Transfer Objects (DTOs) for encapsulating request data.
-   `com.example.dok.exception`: Custom exception classes and a global exception handler.
-   `com.example.dok.model`: The `MarkdownFile` JPA entity.
-   `com.example.dok.repository`: The Spring Data JPA repository for database operations.
-   `com.example.dok.service`: The `MarkdownService` containing the core business logic.
-   `src/test`: A suite of unit tests built with JUnit 5 and Mockito.

---

## Core Components & Design Patterns

-   **Controller Layer**: Handles HTTP requests, validates input, and delegates to the service layer. It is responsible for returning `ResponseEntity` objects with appropriate HTTP status codes.
-   **Service Layer**: Contains the core business logic. It throws custom, specific exceptions for error conditions.
-   **Global Exception Handler**: A `@RestControllerAdvice` class that catches exceptions thrown from the service layer and translates them into consistent, user-friendly `ResponseEntity` error messages.
-   **Records for DTOs**: Request bodies are encapsulated in immutable Java `record` classes for clarity and safety.
-   **Builder Pattern**: The `MarkdownFile` entity uses the Builder pattern for clean and readable object instantiation.

---

## Error Handling Strategy

The backend uses a centralized exception handling mechanism to ensure consistent and meaningful API responses.

1.  **Custom Exceptions**: The service layer throws specific, unchecked exceptions (e.g., `ResourceNotFoundException`, `InvalidRequestException`) when an operation cannot be completed.
2.  **`@RestControllerAdvice`**: A global exception handler intercepts these custom exceptions.
3.  **`ResponseEntity`**: The handler converts the caught exception into a `ResponseEntity` with a precise HTTP status code (e.g., `404 Not Found`, `400 Bad Request`) and a JSON body containing the error message.

This approach keeps the controller and service layers clean of error-handling boilerplate and provides a predictable, RESTful experience for the client.

---

## API Endpoints

| Method   | Path                  | Request Body (JSON)        | Success Response                                   | Error Responses                                       |
| :------- | :-------------------- | :------------------------- | :------------------------------------------------- | :---------------------------------------------------- |
| `GET`    | `/list`               | *N/A*                      | `200 OK` with a list of `FileEntry` objects.       | `404 Not Found`                                       |
| `GET`    | `/view`               | *N/A*                      | `200 OK` with the rendered HTML content.           | `404 Not Found`                                       |
| `GET`    | `/raw`                | *N/A*                      | `200 OK` with the raw Markdown content.            | `404 Not Found`                                       |
| `POST`   | `/save`               | `UpdateFileContentRequest` | `200 OK` with a success message.                   | `400 Bad Request`, `404 Not Found`                    |
| `POST`   | `/create-file`        | *N/A*                      | `201 Created` with a success message.              | `400 Bad Request`                                     |
| `POST`   | `/create-directory`   | *N/A*                      | `201 Created` with a success message.              | `400 Bad Request`                                     |
| `POST`   | `/move`               | `MoveFileRequest`          | `200 OK` with a success message.                   | `400 Bad Request`, `404 Not Found`                    |
| `DELETE` | `/delete`             | *N/A*                      | `200 OK` with a success message.                   | `400 Bad Request`, `404 Not Found`                    |

---

## Key Dependencies

-   **Spring Boot Starter Web**: For building the RESTful API.
-   **Spring Boot Starter Data JPA**: To simplify database access.
-   **H2 Database**: An in-memory database perfect for development and testing.
-   **Flexmark**: A high-performance Java library for parsing Markdown to HTML.
-   **Lombok**: To reduce boilerplate code with annotations like `@Builder`.
