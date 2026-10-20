# StockPulse — Architecture Decision Record

## 1. Commerce logic placement
- **Context:** Product endpoints must stay small while rules and AI evolve.
- **Options:** Put rules in ProductService or separate advisors.
- **Decision:** Dedicated CommerceAdvisor interface and implementations, with a routing adapter.
- **Tradeoffs:** More classes, but simpler testing and future strategy additions.

## 2. Separate pricing and replenishment recommendations
- **Context:** Price and replenishment need distinct outputs and safety checks.
- **Options:** One combined LLM call or independent calls.
- **Decision:** Separate advisor methods and prompts, independently validated.
- **Tradeoffs:** Potentially two API calls; one can fall back without invalidating the other.

## 3. Runtime strategy selection
- **Context:** Operators need deterministic rules when AI is unavailable.
- **Options:** Restart-time bean selection or runtime routing.
- **Decision:** AdvisorRouter switches RULE/AI via /api/advisor/mode without restarting.
- **Tradeoffs:** In-memory mode resets to RULE on restart; production would persist per-tenant settings.

## 4. LLM failure and safety
- **Context:** Network failures and malformed suggestions cannot block inventory updates.
- **Options:** Fail the operation or use deterministic rules.
- **Decision:** Short HTTP timeouts, parsed/validated JSON, rule-based fallback in GeminiCommerceAdvisor.
- **Tradeoffs:** Fallback output may be less context-aware; source-of-recommendation is not separately persisted.

## 5. Event-driven human-in-the-loop
- **Context:** Stock/order writes should return before AI reasoning finishes.
- **Options:** Synchronous calls, scheduled polling, or after-commit events.
- **Decision:** Publish InventoryChangedEvent in transactions, process AFTER_COMMIT with @Async,
  deduplicate PENDING suggestions per product/trigger/type, and require ACCEPTED to apply changes.
- **Tradeoffs:** In-process async events are not durable across process crashes; production should use an outbox/queue.

## 6. Frontend architecture and full-stack integration
- **Context:** Merchandisers need a dashboard to monitor stock, review recommendations, and approve or reject suggested actions without manually calling REST APIs.
- **Options:** Spring Boot server-rendered templates, a single-component React prototype, or a modular React application.
- **Decision:** Build a React 18 + Vite dashboard using reusable components for inventory, metrics, advisor selection, and recommendation approvals. Centralize HTTP requests in `src/lib/api.js` and shared dashboard state in the `useDashboard` hook.
- **Tradeoffs:** Separate frontend deployment and additional dependencies, but clearer separation of concerns, maintainability, and a better operator experience.

## 7. Frontend data synchronization
- **Context:** Inventory updates and asynchronous AI recommendations must become visible in the dashboard without requiring manual refresh.
- **Options:** Manual refresh, periodic polling, WebSockets, or Server-Sent Events (SSE).
- **Decision:** Poll backend APIs every four seconds using `useDashboard`. Use Vite's development proxy to forward `/api` requests to Spring Boot.
- **Tradeoffs:** Polling is straightforward and reliable for a hackathon demo but introduces redundant requests and up to four seconds of UI update latency. SSE is preferable for a production system with frequent updates.

## 8. Human approval and transactional consistency
- **Context:** AI recommendations should support operator decisions without directly changing product prices or inventory.
- **Options:** Automatically apply AI recommendations or require explicit human approval.
- **Decision:** Display `PENDING` pricing and reorder suggestions in the React Decision Center. Approval requests are sent to the backend, which applies changes transactionally. Rejected suggestions do not modify products.
- **Tradeoffs:** Manual approval introduces operational overhead but improves control, traceability, and protection against incorrect AI recommendations.

## 9. Extensibility and deliberate exclusions
- **Context:** Limited sprint time requires prioritizing an end-to-end functional workflow over production infrastructure.
- **Decision:** Implement the core Spring Boot backend, pluggable RULE/AI advisor, asynchronous inventory triggers, and React dashboard. Defer SSE, competitor scraping, margin-floor enforcement, supplier integration, authentication, and production deployment. Future advisors can implement `CommerceAdvisor`; durable events and richer demand history are future enhancements.
- **Tradeoffs:** The application demonstrates the complete inventory-to-recommendation-to-approval workflow, but its event delivery, security, and data synchronization would need strengthening for production use.