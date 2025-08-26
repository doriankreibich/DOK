# Backend Technical Guide

## Overview

The backend for DOK is a Spring Boot application responsible for all business logic, including file system operations, database interactions, and Markdown-to-HTML conversion. It exposes a RESTful API that the JavaScript frontend consumes.

---

## Project Structure

The backend source code is organized into the following key packages:

-   `com.example.dok.config`: Contains Spring configuration classes, such as `MarkdownConfig` for setting up the Markdown parser.
-   `com.example.dok.controller`: Holds the `MarkdownController`, which defines the REST API endpoints and handles incoming HTTP requests.
-   `com.example.dok.dto`: Contains Data Transfer Objects (DTOs), like `FileEntry`, used for structuring data sent to the client.
-   `com.example.dok.model`: Defines the JPA entity (`MarkdownFile`) that maps to the database table.
-   `com.example.dok.repository`: Includes the Spring Data JPA repository interface (`MarkdownFileRepository`) for database operations.
-   `com.example.dok.service`: Contains the core business logic in the `MarkdownService` class, which is called by the controller.

---

## Core Components

-   **`MarkdownController`**: The entry point for all API requests. It delegates all business logic to the `MarkdownService` and returns the results to the client.
-   **`MarkdownService`**: The heart of the backend. It contains all the business logic for handling file operations, such as creating, saving, moving, and deleting files and directories. It is marked as `@Transactional` to ensure data integrity during database operations.
-   **`MarkdownFileRepository`**: A JPA repository that provides an abstraction layer over the database. It includes custom queries for finding and deleting files by path.
-   **`MarkdownConfig`**: Configures the Flexmark `Parser` and `HtmlRenderer` beans, enabling support for various Markdown extensions like tables, task lists, and wikilinks.

---

## API Endpoints

All endpoints are prefixed by the application's context path.

| Method   | Path                  | Parameters                               | Description                                                                        |
| :------- | :-------------------- | :--------------------------------------- | :--------------------------------------------------------------------------------- |
| `GET`    | `/list`               | `path` (optional, default: `/`)          | Lists the files and directories directly under the given path.                     |
| `GET`    | `/view`               | `path`                                   | Renders the Markdown content of a file to HTML.                                    |
| `GET`    | `/raw`                | `path`                                   | Retrieves the raw Markdown content of a file.                                      |
| `POST`   | `/save`               | `path`, `content`                        | Saves or updates the content of a Markdown file.                                   |
| `POST`   | `/create-file`        | `path`                                   | Creates a new, empty Markdown file at the specified path.                          |
| `POST`   | `/create-directory`   | `path`                                   | Creates a new directory at the specified path.                                     |
| `POST`   | `/move`               | `source`, `destination`                  | Moves a file or directory from the source path to the destination path.            |
| `DELETE` | `/delete`             | `path`                                   | Deletes a file or an entire directory (including its children).                    |

---

## Business Logic Details

-   **Path Normalization**: A private `normalizePath` method ensures that all file paths are clean, consistent, and free of duplicate slashes or trailing slashes.
-   **Transactional Operations**: All methods that modify the database (`save`, `create`, `move`, `delete`) are annotated with `@Transactional`. This guarantees that if any part of an operation fails, the entire transaction is rolled back, preventing the database from being left in an inconsistent state.
-   **Directory Logic**: Operations on directories (like `move` and `delete`) correctly handle all child files and subdirectories recursively.

---

## Data Model

The application uses a single JPA entity, `MarkdownFile`, to represent both files and directories in the database. This is stored in an H2 in-memory database.

-   `id`: The primary key.
-   `path`: The full, unique path to the file or directory (e.g., `/docs/guide.md`).
-   `name`: The name of the file or directory (e.g., `guide.md`).
-   `isDirectory`: A boolean flag that is `true` for directories and `false` for files.
-   `content`: A large text field (`@Lob`) that stores the Markdown content for files. It is `null` for directories.

---

## Key Dependencies

-   **Spring Boot Starter Web**: Provides support for building web applications, including the RESTful API.
-   **Spring Boot Starter Data JPA**: Simplifies database access with JPA and Hibernate.
-   **H2 Database**: An in-memory database used for storing file system data.
-   **Flexmark**: A high-performance Java library for parsing Markdown and rendering it to HTML, with support for numerous extensions.
-   **Lombok**: Reduces boilerplate code by automatically generating getters, setters, constructors, etc.
