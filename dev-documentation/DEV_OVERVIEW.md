Rosetta-Java Cardano Application — Development Guide (Context Window For AI Agents)

✅ Overview

This project is a Java implementation of the Rosetta specification, adapted from Coinbase’s original mesh. It targets the Cardano blockchain and is optimized for performance, scalability, and testability.

⸻

🛠️ Technologies & Standards
•	Java 25 – Latest LTS features leveraged.
•	Lombok – For reducing boilerplate (e.g., getters, constructors).
•	Hibernate + JOOQ – A hybrid approach planned:
•	Hibernate for standard ORM tasks.
•	JOOQ for complex SQL operations.
•	Hibernate for standard ORM needs.
•	JOOQ for high-performance, complex SQL queries.
•	Flywheel – Used for event-sourced state and domain logic.
•	OpenAPI (api.yaml) – Contract-first approach:
•	Edit src/main/resources/rosetta-specifications-1.4.15/api.yaml for API changes.
•	Code generation tools automatically create request/response classes.
•	No Spring RestControllers – All endpoints are generated from OpenAPI spec; avoid manually annotating endpoints.
•	JUnit 5 with @Nested tests – Organize tests into logical groups.
•	AssertJ – Preferred assertion framework for fluent and expressive assertions.

⸻

📋 Step-by-Step Context Window / Discovery

1. Rosetta API Specification
   •	Maintained in:
   src/main/resources/rosetta-specifications-1.4.15/api.yaml
   •	❗ All endpoints and models are generated, do not manually modify controller classes.

2. Code Generation
   •	Triggered via build tools (Gradle/Maven).
   •	Output includes:
   •	DTOs
   •	Server interfaces
   •	Clients
   •	Ensures OpenAPI remains the single source of truth.

3. Persistence Layer
   •	Hibernate handles standard CRUD and entity management.
   •	JOOQ will be used for:
   •	Bulk operations
   •	Deep filtering
   •	Custom joins / window functions
   •	Both work seamlessly alongside Flywheel event sourcing.

4. Domain Logic with Flywheel
   •	Domain state transitions are event-driven.
   •	Promotes traceability and immutability.

5. Testing Strategy
   •	JUnit 5 with @Nested classes to:
   •	Separate concerns within test files.
   •	Improve readability and scope clarity.
   •	AssertJ provides fluent API for:
   •	Clean, expressive assertions.
   •	Better failure messages than vanilla JUnit or Hamcrest.

⸻

📎 Notes
•	Avoid using @RestController or manual API layer.
•	All changes to API contract must be made in api.yaml.
•	Lombok annotations (@Value, @Builder, @Getter, etc.) are preferred to minimize boilerplate.