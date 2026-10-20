
# AI Usage Log — StockPulse

## Development Tool
- IDE: VS Code
- AI Assistant: Cline
- Model: Ollama-backed model (exact model not confirmed)

## 1. Architecture Planning
- Used AI to propose the initial package structure, entities, strategy pattern, APIs, and event flow.
- Reviewed the design and identified changes needed to align with the assignment.

## 2. Backend Setup
- Used AI to create the Spring Boot backend with Java, Maven, JPA, Validation, and H2.
- Chose file-based H2 to avoid external database setup during the hackathon.
- Verification reported by Cline: compilation, health endpoint, and existing test passed.

## 3. Domain Modeling
- Used AI to generate Product, InventorySnapshot, PricingSuggestion, ReorderSuggestion, enums, and repositories.
- Used BigDecimal for monetary values and string-based enum persistence.
- Verification reported by Cline: compilation and existing tests passed.

## Next
- Implement product and order APIs.
- Implement commerce strategies and AI integration.
- Implement event-driven suggestions and human approval.
