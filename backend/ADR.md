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

## 6. Extensibility and deliberate exclusions
- **Context:** Limited sprint time favors a complete backend loop.
- **Decision:** Defer React UI, SSE, competitor scraping, margin floor, supplier integration, and deployment.
  Future advisors implement CommerceAdvisor; durable events and richer demand history are Sprint 2 work.
- **Tradeoffs:** Demonstration uses HTTP requests rather than a merchandiser console.
