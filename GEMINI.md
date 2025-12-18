# Lattice (晶格) - Project Context

## Project Overview

**Lattice** is an "engineering approach to life management" system. It applies Data Governance principles to personal life data (career, finance, hobbies), treating them as structured nodes in a graph-like structure. It leverages **Spring AI** and **Vector Embeddings** to provide intelligent retrieval and agentic workflows.

**Core Philosophy:** Structured Chaos. Storing dynamic attributes in **PostgreSQL JSONB** alongside **pgvector** embeddings for semantic understanding.

### Tech Stack

*   **Backend:** Java 21, Spring Boot 3.3.4, Spring AI 1.1.2
*   **Frontend:** React 19, TypeScript, Vite 6, Ant Design Pro
*   **Database:** PostgreSQL 16 (with `pgvector` extension)
*   **Infrastructure:** Docker & Docker Compose
*   **Integrations:** Firefly III (Finance), ntfy (Notifications), DeepSeek (LLM)

## Architecture

The project is a full-stack application containerized via Docker.

### Backend (`lattice-oss/`)
A Maven multi-module project:
*   `lattice-core`: Domain entities, Repository layer, Database migrations (Flyway).
*   `lattice-ingestion`: Logic for ingesting data (parsers, etc.).
*   `lattice-retrieval`: RAG (Retrieval-Augmented Generation) logic.
*   `lattice-agent`: AI Agent logic, Prompt management, Tool calling.
*   `lattice-application`: Main Spring Boot application entry point and configuration.

### Frontend (`web-react/`)
A Modern React application:
*   Built with **Vite**.
*   Uses **Ant Design** components.
*   Managed via `npm`/`yarn`.

### Data Storage
*   **PostgreSQL**: Primary store for both relational data and vector embeddings.
*   **Flyway**: Manages database schema migrations found in `lattice-core/src/main/resources/db/migration`.

## Key Directories

*   `lattice-oss/`: Java backend source code.
*   `web-react/`: React frontend source code.
*   `lattice-python-engine/`: Python-based AI worker (Note: Currently not in `docker-compose.yml`).
*   `sql/`: Database initialization scripts.
*   `scripts/`: Deployment and utility scripts.
*   `docs/`: Project documentation.

## Building and Running

### Quick Start (Docker)
The easiest way to run the entire stack is using the provided script:

```bash
./scripts/deploy.sh
```

This will spin up:
*   Backend: `http://localhost:8080`
*   Frontend: `http://localhost:4173`
*   Postgres: `localhost:25432`
*   PgAdmin: `http://localhost:25050`
*   Ntfy: `http://localhost:25862`
*   Prometheus/Grafana: for monitoring.

### Local Development

#### Prerequisites
*   Java 21 SDK
*   Node.js & npm
*   Docker (for running dependent services like Postgres if not installed locally)

#### Backend
1.  Navigate to `lattice-oss`:
    ```bash
    cd lattice-oss
    ```
2.  Build the project:
    ```bash
    mvn clean install
    ```
3.  Run the application (ensure database is running and configured):
    *   Main Class: `com.lattice.core.LatticeCoreApplication`
    *   Check `application.yml` for required environment variables (especially `DEEPSEEK_API_KEY`).

#### Frontend
1.  Navigate to `web-react`:
    ```bash
    cd web-react
    ```
2.  Install dependencies:
    ```bash
    npm install
    ```
3.  Start dev server:
    ```bash
    npm run dev
    ```

## Configuration

### Environment Variables
Key variables utilized in `docker-compose.yml` and `application.yml`:

*   `DEEPSEEK_API_KEY`: API Key for the LLM provider.
*   `FIREFLY_API_TOKEN`: Token for Firefly III integration.
*   `LATTICE_DB_*`: Database connection details.
*   `NTFY_*`: Notification service configuration.

### Database Migrations
Database changes are handled by **Flyway**.
*   Location: `lattice-oss/lattice-core/src/main/resources/db/migration`
*   Format: `V{VERSION}__{DESCRIPTION}.sql`
*   Do not modify existing migration files; create new ones for schema changes.

## Development Conventions

*   **Java**: Follows standard Spring Boot layering (Controller -> Service -> Repository). Uses Lombok for boilerplate reduction.
*   **AI/LLM**: Logic is encapsulated in `lattice-agent`. Prompts are managed via `lattice.prompts` configuration or external files.
*   **React**: Functional components with Hooks. TypeScript is strictly enforced.

## Notes & Troubleshooting

*   **Python Worker**: The `application.yml` references a `python-worker` service, but it is currently missing from `docker-compose.yml`. AI features relying on this might need manual setup or are work-in-progress.
*   **Security**: The project uses JWT for authentication (`lattice.security.jwt`). Ensure secrets are changed in production.
