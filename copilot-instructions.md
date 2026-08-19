# Copilot Instructions

<!--
This file is meant to be a lightweight, editable starting point for agents and contributors.
If the project conventions change, update this document first so future edits stay consistent.
-->

## Project snapshot
- Java 21 / Spring Boot 3.5.x application.
- Gradle build with OpenAPI-generated sources.
- Main package: `org.opendevstack.projects_info_service`.
- Shared IntelliJ code style scheme: `codeStyles/intellij/codeStyles.xml`.
- Max line length: 120 characters.
- `codeStyles/` is reserved for IDE-specific code style exports such as `codeStyles/eclipse/` or
  `codeStyles/visualStudio/`.

## Before changing code
- Read this file first.
- Inspect nearby classes and tests before editing anything.
- Prefer the smallest safe change.
- Do not edit generated files under `target/generated-sources/` unless the task explicitly requires it.

## Java style observed in this repo
- Use 4-space indentation.
- Keep opening braces on the same line.
- Keep line length at or below 120 characters.
- Prefer `var` for local variables when the type is obvious.
- Do not use wildcard imports.
- Group imports by standard Java, third-party libraries, then project classes.
- Keep methods and classes easy to scan; prefer small helper methods over large blocks.
- Use descriptive names for variables, methods, and tests.
- Follow existing line wrapping in surrounding code instead of reformatting unrelated lines.

## Architecture conventions
- Keep package boundaries clear: API layer, service layer, and integration/repository layer should not leak concerns.
- Keep controllers thin: request/response mapping and validation in controllers, business logic in services.
- Create facades in case of need.
- Prefer constructor injection; avoid field injection.
- Keep DTOs and domain/service models separated when contracts diverge.
- Isolate external system details behind dedicated client/service classes.
- Reuse existing error-handling patterns (exception types, mappers, and status translation).

## API rules
- Treat OpenAPI contracts as source of truth for public API behavior.
- Use OpenAPI definitions to generate clients, do not create custom clients.
- Keep request/response schemas backward compatible unless the change is explicitly versioned.
- Validate all incoming inputs and return consistent error responses.
- Do not expose internal exception details, stack traces, tokens, credentials, or infrastructure metadata in API output.
- When changing endpoints, update related OpenAPI definitions and contract tests in the same change.

## Common code patterns
- Lombok is used heavily for boilerplate reduction.
- Builder style is common for DTO/model construction.
- Streams, lambdas, and `Optional` are used frequently.
- Logging uses SLF4J.
- Always use parameterized logging (for example, `log.info("Project {} loaded", projectId)`).
- NEVER log secrets or sensitive values (tokens, passwords, API keys, auth headers, private keys, PII).
- Preserve current null-handling behavior unless a bug fix requires a change.

## Test conventions
- Test framework: JUnit 5 (Jupiter).
- Mocking: Mockito with `@ExtendWith(MockitoExtension.class)`.
- Assertions: AssertJ.
- Test naming commonly follows `given...when...then...` style.
- Test body commonly contains `// given // when // then` blocks.
- Test data helpers often use the `Mother` pattern.
- Keep tests focused on behavior and readable without extra abstraction.

## Build and dependency notes
- `build.gradle` is the source of truth for Java version, Spring Boot, testing dependencies, and OpenAPI generation.
- If a change affects API contracts, check whether generated sources or OpenAPI definitions also need updates.
- If you are editing formatting-related files, keep this document and the IntelliJ scheme in sync.

## When in doubt
- Match the style of the nearest existing class or test.
- Prefer clarity over cleverness.
- If conventions are unclear, add a brief comment rather than rewriting large sections.

<!--
Maintainers: feel free to extend this file with project-specific rules
such as package boundaries, naming preferences, or release/build steps.
-->
