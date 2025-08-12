# DOK Backend Architecture

This document provides a precise technical specification of the Spring Boot backend for the DOK Markdown editor.

---

## 1. Overview

The backend is a Java-based Spring Boot application responsible for all file system operations. It exposes a REST API that the JavaScript frontend consumes to list, read, create, update, and move files and directories within a predefined root notes directory. It does not have a database; all operations are performed directly on the local file system.

---

## 2. Core Components & Classes

### `DokApplication.java`
- **Purpose:** Standard Spring Boot main entry point.
- **Functionality:** Bootstraps the entire application using `@SpringBootApplication`. No custom logic is present in this file.

### `MarkdownConfig.java`
- **Purpose:** Provides application-wide configuration, specifically the root path for file storage.
- **Key Beans:**
    - `notesDir()`:
        - **Type:** `Bean`
        - **Returns:** `Path`
        - **Functionality:** Defines the root directory where all notes and subdirectories are stored. It is hardcoded to `C:/Users/Dorian/IdeaProjects/DOK/notes`. **Any change to the notes''' storage location must be made here.**

### `MarkdownFile.java`
- **Purpose:** A simple Java `record` that acts as a Data Transfer Object (DTO) for file and directory information.
- **Fields:**
    - `name`: `String` - The name of the file or directory.
    - `path`: `String` - The unique, relative path of the file from the `notesDir` root.
    - `isDirectory`: `boolean` - `true` if the object represents a directory, `false` if it is a file.
    - `children`: `List<MarkdownFile>` - A list of child files and directories, used to build the tree structure.

### `MarkdownFileRepository.java`
- **Purpose:** This is the core service class containing all business logic for file system manipulation. It directly interacts with `java.nio.file` APIs.
- **Key Methods:**
    - `findAll()`:
        - **Returns:** `List<MarkdownFile>`
        - **Logic:** Recursively walks the file tree starting from the `notesDir` path provided by `MarkdownConfig`. It builds and returns a complete hierarchical list of all files and directories.
    - `findContent(String path)`:
        - **Parameters:** `path` - The relative path of the file.
        - **Returns:** `String`
        - **Logic:** Reads and returns the entire content of the specified file as a single string.
    - `save(String path, String content)`:
        - **Parameters:** `path` - The relative path of the file; `content` - The new text to write.
        - **Logic:** Writes the provided `content` to the specified file. If the file does not exist, it is created. If it does exist, its content is overwritten.
    - `create(String path)`:
        - **Parameters:** `path` - The relative path for the new file or directory.
        - **Logic:** Creates a new file or directory based on the path. It intelligently creates a directory if the path ends with a `/` and a file otherwise.
    - `move(String fromPath, String toPath)`:
        - **Parameters:** `fromPath` - The source path; `toPath` - The destination path.
        - **Logic:** Moves a file or directory from the `fromPath` to the `toPath`.

### `MarkdownController.java`
- **Purpose:** A standard Spring `@RestController` that exposes the functionality of the `MarkdownFileRepository` as a public REST API.
- **API Endpoints:**
    - **`GET /api/files`**
        - **Calls:** `repository.findAll()`
        - **Returns:** `List<MarkdownFile>` - A JSON array representing the entire file tree.
    - **`GET /api/files/content`**
        - **Request Parameter:** `path` (String)
        - **Calls:** `repository.findContent(path)`
        - **Returns:** `String` - The raw text content of the requested file.
    - **`POST /api/files/content`**
        - **Request Body:** A `Map<String, String>` containing `"path"` and `"content"`.
        - **Calls:** `repository.save(path, content)`
        - **Returns:** `void` - Responds with HTTP 200 OK on success.
    - **`POST /api/files`**
        - **Request Body:** A `Map<String, String>` containing `"path"`.
        - **Calls:** `repository.create(path)`
        - **Returns:** `void` - Responds with HTTP 200 OK on success.
    - **`POST /api/files/move`**
        - **Request Body:** A `Map<String, String>` containing `"from"` and `"to"`.
        - **Calls:** `repository.move(from, to)`
        - **Returns:** `void` - Responds with HTTP 200 OK on success.
---