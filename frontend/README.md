# StockPulse — React dashboard

A modular React 18 + Vite frontend for the existing Spring Boot StockPulse backend.

## Structure
- `src/App.jsx`: page composition and navigation
- `src/components/Sidebar.jsx`, `Header.jsx`: app shell
- `src/components/StatCards.jsx`: KPI summaries
- `src/components/AdvisorPanel.jsx`: RULE / Gemini AI mode switch
- `src/components/InventoryTable.jsx`: searchable inventory, categories, sale simulation, manual suggestions
- `src/components/DecisionQueue.jsx`: pricing/replenishment cards, confidence, reasoning, accept/reject
- `src/components/AddProductModal.jsx`: product creation
- `src/hooks/useDashboard.js`: polling, state, loading and API actions
- `src/lib/api.js`: typed-by-purpose REST helpers
- `src/styles.css`: responsive dashboard design

## Setup
1. Extract `frontend/` into your repository root, beside `backend/`.
2. Run Spring Boot on port `8080`.
3. In a second terminal:
   ```powershell
   cd frontend
   npm install
   npm run dev
   ```
4. Open http://localhost:5173

Vite proxies `/api` to `http://localhost:8080`; no backend CORS changes needed.

## Demo
Create a product with stock `12`, reorder threshold `10`, price `100`, then click **Sell** for 5 units. Stock becomes `7`; wait 4 seconds for low-inventory suggestions. Approve pricing or reorder and watch the table update. Switch advisor mode to Gemini only if your backend has a valid `LLM_API_KEY`.

## Important
This frontend uses the backend routes in the previously shared StockPulse backend-only patch. No mock data or fabricated AI results. The frontend build and backend integration should be verified locally before presenting.
