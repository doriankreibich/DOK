### Implemented Core Features

*   **[x] Core Application Architecture:**
    *   [x] Three-Tier Structure (Controller, Service, Repository).
    *   [x] Dependency Injection with Spring Beans.
*   **[x] RESTful API for File Management:**
    *   [x] CRUD operations for files and directories (`/list`, `/create-file`, `/save`, `/move`, `/delete`).
    *   [x] Consistent API responses using `ResponseEntity`.
*   **[x] Markdown Processing:**
    *   [x] Server-side rendering of Markdown to HTML using `flexmark-java`.
    *   [x] Endpoints for both rendered HTML (`/view`) and raw content (`/raw`).
*   **[x] Data Persistence:**
    *   [x] Storing file system structure in a database using Spring Data JPA.
    *   [x] H2 in-memory database for local development.
*   **[x] Robust Error Handling:**
    *   [x] Centralized exception handling with `@ControllerAdvice`.
    *   [x] Custom exceptions (`ResourceNotFoundException`, etc.) for clear error states.
*   **[x] Basic Search Functionality:**
    *   [x] A `GET /search` endpoint powered by JPA query methods (SQL `LIKE`).
*   **[x] Initial Setup & Configuration:**
    *   [x] Database initialization on startup (`CommandLineRunner`).
    *   [x] Externalized configuration for Markdown parsing.

---

###  Future Implementation Plan

#### 1. User Authentication & Role System (Spring Security)

*   **Step 1: Core Setup**
    *   Add `spring-boot-starter-security` and JWT dependencies (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`) to `pom.xml`.
    *   Create a main `SecurityConfig` class with a `SecurityFilterChain` bean.

*   **Step 2: Data Model**
    *   Create a `User` entity (`@Entity`) with fields: `id`, `username`, `password`, `email`, and a `Set<Role> roles`.
    *   Create a `Role` entity with fields: `id`, `name` (e.g., `ROLE_USER`, `ROLE_ADMIN`).
    *   Define the Many-to-Many relationship between `User` and `Role`.
    *   Create `UserRepository` and `RoleRepository` interfaces.

*   **Step 3: Authentication Logic**
    *   Create a `CustomUserDetailsService` that implements `UserDetailsService` to load users from the database.
    *   Define a `PasswordEncoder` bean (using `BCryptPasswordEncoder`) in `SecurityConfig`.

*   **Step 4: JWT (JSON Web Token) Implementation**
    *   Create a `JwtUtil` or `JwtTokenProvider` class to generate, validate, and parse JWTs.
    *   Create a `JwtAuthenticationFilter` that runs once per request to validate the token from the `Authorization` header and set the user's authentication context.
    *   Add this filter to your `SecurityFilterChain`.

*   **Step 5: API Endpoints & Authorization**
    *   Create an `AuthController` with public endpoints:
        *   `POST /api/auth/register`: To create a new user (hash password, assign default role).
        *   `POST /api/auth/login`: To authenticate a user and return a JWT.
    *   In `SecurityConfig`, secure your existing `MarkdownController` endpoints using `http.authorizeHttpRequests()`. For example:
        *   Permit access to `/api/auth/**` and read-only endpoints (`/view`, `/list`).
        *   Require authentication for write endpoints (`/save`, `/create-file`, `/delete`).
        *   Restrict certain endpoints to specific roles (e.g., `hasRole('ADMIN')`).

*   **Step 6: Exception Handling**
    *   Update `GlobalExceptionHandler` to handle `AccessDeniedException` (403 Forbidden) and `AuthenticationException` (401 Unauthorized).

#### 2. Advanced Search Implementation (Elasticsearch)

*   **Step 1: Setup & Dependencies**
    *   Create a `docker-compose.yml` file to run an Elasticsearch instance locally.
    *   Add the `spring-boot-starter-data-elasticsearch` dependency to `pom.xml`.
    *   Configure the Elasticsearch connection URI in `application.properties`.

*   **Step 2: Elasticsearch Data Model**
    *   Create a `MarkdownDocument` class annotated with `@Document(indexName = "markdown_files")` to represent data in Elasticsearch.
    *   Define fields like `id`, `path`, and `content` using `@Field`.
    *   Create a `MarkdownSearchRepository` interface that extends `ElasticsearchRepository`.

*   **Step 3: Data Synchronization Logic**
    *   Modify the `MarkdownService` to keep the Elasticsearch index synchronized with the main database on every CUD (Create, Update, Delete) operation.
    *   On `create`/`save`: Also save the document to `MarkdownSearchRepository`.
    *   On `delete`: Also delete the document from `MarkdownSearchRepository`.
    *   On `move`: Update the path in the corresponding Elasticsearch document.

*   **Step 4: Initial Data Indexing**
    *   Create a `CommandLineRunner` bean that runs on application startup.
    *   This runner will fetch all existing files from the main database and index them into Elasticsearch, ensuring the search index is complete.

*   **Step 5: Implement Search Endpoint**
    *   Update the `search()` method in `MarkdownService` to query the `MarkdownSearchRepository` instead of the JPA repository. The `GET /search` endpoint will now be powered by Elasticsearch.

#### 3. Platform Enhancements

*   **Content Versioning & History**
    *   **Goal:** Never lose a change. Allow users to view, compare, and revert to previous versions of a document.
    *   **Implementation:**
        *   Create a `MarkdownFileHistory` entity to store snapshots of content on each save.
        *   Add API endpoints like `GET /history?path=...` to list versions and `POST /revert?versionId=...` to restore an old version.

*   **Image & Attachment Uploads**
    *   **Goal:** Allow users to upload images and other files and embed them in their documents.
    *   **Implementation:**
        *   Create a `POST /upload` endpoint that accepts multipart file data.
        *   Store files either on the server's filesystem or in the database (as a BLOB).
        *   Create a `GET /attachments/{filename}` endpoint to serve the uploaded files.

*   **API Documentation (Swagger/OpenAPI)**
    *   **Goal:** Automatically generate interactive API documentation from your code.
    *   **Implementation:**
        *   Add the `springdoc-openapi-starter-webmvc-ui` dependency.

*   **Database Migrations (Flyway)**
    *   **Goal:** Manage database schema changes in a safe, versioned, and automated way.
    *   **Implementation:**
        *   Add the `flyway-core` dependency.
        *   Create SQL migration scripts in `src/main/resources/db/migration`.

*   **Caching Layer**
    *   **Goal:** Improve performance and reduce database load by caching frequently accessed data.
    *   **Implementation:**
        *   Add the `spring-boot-starter-cache` dependency and enable caching with `@EnableCaching`.
        *   Use the `@Cacheable` annotation on slow or frequently called service methods, like `viewMarkdown()`.

#### 4. User Experience Features

*   **Tagging System for Organization**
    *   **Goal:** Allow users to categorize documents with tags for better organization and discovery.
    *   **Implementation:**
        *   Create a `Tag` entity and a Many-to-Many relationship with `MarkdownFile`.
        *   Add API endpoints to add/remove tags and to find all documents with a specific tag.

*   **Commenting on Documents**
    *   **Goal:** Enable discussions and feedback directly on document pages.
    *   **Implementation:**
        *   Create a `Comment` entity with relationships to `User` and `MarkdownFile`.
        *   Add REST endpoints for creating, reading, updating, and deleting comments.

*   **Comprehensive Testing Strategy**
    *   **Goal:** Ensure application stability and reliability by testing the integration between different layers.
    *   **Implementation:**
        *   Write **Integration Tests** (`@SpringBootTest`) that use `Testcontainers` to spin up real database and Elasticsearch instances.

*   **Asynchronous Task Processing**
    *   **Goal:** Improve responsiveness by offloading long-running operations to background threads.
    *   **Implementation:**
        *   Enable async support with `@EnableAsync`.
        *   Use the `@Async` annotation on service methods for tasks like re-indexing or generating large exports.


### Feature Roadmap

#### 1. User Authentication & Role System (Spring Security)

*   **Step 1: Core Setup**
    *   Add `spring-boot-starter-security` and JWT dependencies (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`) to `pom.xml`.
    *   Create a main `SecurityConfig` class with a `SecurityFilterChain` bean.

*   **Step 2: Data Model**
    *   Create a `User` entity (`@Entity`) with fields: `id`, `username`, `password`, `email`, and a `Set<Role> roles`.
    *   Create a `Role` entity with fields: `id`, `name` (e.g., `ROLE_USER`, `ROLE_ADMIN`).
    *   Define the Many-to-Many relationship between `User` and `Role`.
    *   Create `UserRepository` and `RoleRepository` interfaces.

*   **Step 3: Authentication Logic**
    *   Create a `CustomUserDetailsService` that implements `UserDetailsService` to load users from the database.
    *   Define a `PasswordEncoder` bean (using `BCryptPasswordEncoder`) in `SecurityConfig`.

*   **Step 4: JWT (JSON Web Token) Implementation**
    *   Create a `JwtUtil` or `JwtTokenProvider` class to generate, validate, and parse JWTs.
    *   Create a `JwtAuthenticationFilter` that runs once per request to validate the token from the `Authorization` header and set the user's authentication context.
    *   Add this filter to your `SecurityFilterChain`.

*   **Step 5: API Endpoints & Authorization**
    *   Create an `AuthController` with public endpoints:
        *   `POST /api/auth/register`: To create a new user (hash password, assign default role).
        *   `POST /api/auth/login`: To authenticate a user and return a JWT.
    *   In `SecurityConfig`, secure your existing `MarkdownController` endpoints using `http.authorizeHttpRequests()`. For example:
        *   Permit access to `/api/auth/**` and read-only endpoints (`/view`, `/list`).
        *   Require authentication for write endpoints (`/save`, `/create-file`, `/delete`).
        *   Restrict certain endpoints to specific roles (e.g., `hasRole('ADMIN')`).

*   **Step 6: Exception Handling**
    *   Update `GlobalExceptionHandler` to handle `AccessDeniedException` (403 Forbidden) and `AuthenticationException` (401 Unauthorized).

### Feature Roadmap

#### 1. User Authentication & Role System (Spring Security)

*   **Step 1: Core Setup**
    *   Add `spring-boot-starter-security` and JWT dependencies (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`) to `pom.xml`.
    *   Create a main `SecurityConfig` class with a `SecurityFilterChain` bean.

*   **Step 2: Data Model**
    *   Create a `User` entity (`@Entity`) with fields: `id`, `username`, `password`, `email`, and a `Set<Role> roles`.
    *   Create a `Role` entity with fields: `id`, `name` (e.g., `ROLE_USER`, `ROLE_ADMIN`).
    *   Define the Many-to-Many relationship between `User` and `Role`.
    *   Create `UserRepository` and `RoleRepository` interfaces.

*   **Step 3: Authentication Logic**
    *   Create a `CustomUserDetailsService` that implements `UserDetailsService` to load users from the database.
    *   Define a `PasswordEncoder` bean (using `BCryptPasswordEncoder`) in `SecurityConfig`.

*   **Step 4: JWT (JSON Web Token) Implementation**
    *   Create a `JwtUtil` or `JwtTokenProvider` class to generate, validate, and parse JWTs.
    *   Create a `JwtAuthenticationFilter` that runs once per request to validate the token from the `Authorization` header and set the user's authentication context.
    *   Add this filter to your `SecurityFilterChain`.

*   **Step 5: API Endpoints & Authorization**
    *   Create an `AuthController` with public endpoints:
        *   `POST /api/auth/register`: To create a new user (hash password, assign default role).
        *   `POST /api/auth/login`: To authenticate a user and return a JWT.
    *   In `SecurityConfig`, secure your existing `MarkdownController` endpoints using `http.authorizeHttpRequests()`. For example:
        *   Permit access to `/api/auth/**` and read-only endpoints (`/view`, `/list`).
        *   Require authentication for write endpoints (`/save`, `/create-file`, `/delete`).
        *   Restrict certain endpoints to specific roles (e.g., `hasRole('ADMIN')`).

*   **Step 6: Exception Handling**
    *   Update `GlobalExceptionHandler` to handle `AccessDeniedException` (403 Forbidden) and `AuthenticationException` (401 Unauthorized).

---

#### 2. Advanced Search Implementation (Elasticsearch)

*   **Step 1: Setup & Dependencies**
    *   Create a `docker-compose.yml` file to run an Elasticsearch instance locally.
    *   Add the `spring-boot-starter-data-elasticsearch` dependency to `pom.xml`.
    *   Configure the Elasticsearch connection URI in `application.properties`.

*   **Step 2: Elasticsearch Data Model**
    *   Create a `MarkdownDocument` class annotated with `@Document(indexName = "markdown_files")` to represent data in Elasticsearch.
    *   Define fields like `id`, `path`, and `content` using `@Field`.
    *   Create a `MarkdownSearchRepository` interface that extends `ElasticsearchRepository`.

*   **Step 3: Data Synchronization Logic**
    *   Modify the `MarkdownService` to keep the Elasticsearch index synchronized with the main database on every CUD (Create, Update, Delete) operation.
    *   On `create`/`save`: Also save the document to `MarkdownSearchRepository`.
    *   On `delete`: Also delete the document from `MarkdownSearchRepository`.
    *   On `move`: Update the path in the corresponding Elasticsearch document.

*   **Step 4: Initial Data Indexing**
    *   Create a `CommandLineRunner` bean that runs on application startup.
    *   This runner will fetch all existing files from the main database and index them into Elasticsearch, ensuring the search index is complete.

*   **Step 5: Implement Search Endpoint**
    *   Update the `search()` method in `MarkdownService` to query the `MarkdownSearchRepository` instead of the JPA repository. The `GET /search` endpoint will now be powered by Elasticsearch.

---

#### 3. Beyond Core Features: Next Steps & Enhancements

*   **Content Versioning & History**
    *   **Goal:** Never lose a change. Allow users to view, compare, and revert to previous versions of a document.
    *   **Implementation:**
        *   Create a `MarkdownFileHistory` entity to store snapshots of content on each save.
        *   Add new API endpoints like `GET /history?path=...` to list versions and `POST /revert?versionId=...` to restore an old version.

*   **Image & Attachment Uploads**
    *   **Goal:** Allow users to upload images and other files and embed them in their documents.
    *   **Implementation:**
        *   Create a `POST /upload` endpoint that accepts multipart file data.
        *   Store files either on the server's filesystem or in the database (as a BLOB).
        *   Create a corresponding `GET /attachments/{filename}` endpoint to serve the uploaded files.

*   **API Documentation (Swagger/OpenAPI)**
    *   **Goal:** Automatically generate interactive API documentation from your code, making it easy for frontend developers or other consumers to understand and use your API.
    *   **Implementation:**
        *   Add the `springdoc-openapi-starter-webmvc-ui` dependency.
        *   The documentation will be available at `/swagger-ui.html` automatically.

*   **Database Migrations (Flyway)**
    *   **Goal:** Manage database schema changes in a safe, versioned, and automated way as the application evolves (e.g., when adding User/Role tables).
    *   **Implementation:**
        *   Add the `flyway-core` dependency.
        *   Create SQL migration scripts in `src/main/resources/db/migration` (e.g., `V2__Create_User_And_Role_Tables.sql`). Flyway runs these automatically on startup.

*   **Caching Layer**
    *   **Goal:** Improve performance and reduce database load by caching frequently accessed data.
    *   **Implementation:**
        *   Add the `spring-boot-starter-cache` dependency and enable caching with `@EnableCaching`.
        *   Use the `@Cacheable` annotation on slow or frequently called service methods, like `viewMarkdown()`, to cache the rendered HTML.