# DOK - A Web-Based Markdown Editor

## Project Overview

DOK is a web-based, server-backed Markdown editor designed for a seamless note-taking experience. It features a file explorer for managing notes and a rich editor that supports live previews and custom syntax. The application is built with a Java Spring Boot backend and a vanilla JavaScript frontend.

---

## Core Features & Technical Specifications

This document outlines the specific features of the application. **Any future updates must preserve this existing functionality.**

### 1. Two-Column Layout

The user interface is a stable, two-column layout.

*   **Left Column (File Explorer):** A resizable pane that displays the file system. It has a default width of 250px and can be resized by the user by dragging its right border.
*   **Right Column (Main Content):** This area contains the editor and its controls. It dynamically fills the remaining horizontal space.

### 2. File Explorer

The file explorer on the left provides full file system management within the notes directory.

*   **Display Files and Directories:**
    *   **Functionality:** Lists all files and directories recursively in a tree structure.
    *   **Technical:** On startup, the frontend calls the `GET /api/files` endpoint to fetch the entire file tree.

*   **Open File:**
    *   **Functionality:** Clicking on a file name in the explorer opens its content in the editor.
    *   **Technical:** This triggers a call to `GET /api/files/content?path=<file_path>` to retrieve the file's raw text content, which is then loaded into the editor component.

*   **Create File:**
    *   **Functionality:** A "New File" button prompts the user for a file name and creates a new, empty `.md` file.
    *   **Technical:** This triggers a call to `POST /api/files` with the new file path in the request body.

*   **Create Directory:**
    *   **Functionality:** A "New Directory" button prompts the user for a directory name and creates a new, empty directory.
    *   **Technical:** This triggers a call to `POST /api/files` with the new directory path in the request body.

*   **Drag-and-Drop to Move:**
    *   **Functionality:** Files and directories can be moved by dragging them onto a new parent directory.
    *   **Technical:** This triggers a call to `POST /api/files/move` with the source and destination paths.

### 3. Markdown Editor (Toast UI Editor)

The editor is the central component for content creation.

*   **Library:** The application uses **Toast UI Editor**.
*   **Current State:** The editor is incorrectly configured with two tabs: "Markdown" and "WYSIWYG". This is a known issue to be resolved.
*   **Auto-Saving:**
    *   **Functionality:** Any changes made in the editor are saved automatically after the user pauses typing for a short duration (debounced).
    *   **Technical:** A `keyup` event listener on the editor is debounced. When triggered, it calls the `saveFile()` JavaScript function, which sends the editor's content to the `POST /api/files/content` endpoint.

### 4. Wikilinks

The application supports Obsidian-style `[[wikilink]]` navigation.

*   **Syntax:** Links are written as `[[FileName]]`. The `.md` extension is not included.
*   **Functionality:** Clicking on a wikilink within the editor will open the corresponding file (e.g., `FileName.md`) in the editor.
*   **Technical:** A click event handler on the editor checks if the clicked element is a wikilink. If so, it extracts the file name and calls the internal `loadFile()` JavaScript function, which fetches and displays the content of that file. The link resolution is handled entirely on the client-side. The files are saved with the raw `[[wikilink]]` text.

---
