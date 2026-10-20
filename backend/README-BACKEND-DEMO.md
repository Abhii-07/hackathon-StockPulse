# StockPulse — Backend-only finishing patch

This ZIP contains **only files to add/replace** in your current project. It is not a standalone project.
Paths start at the directory containing `pom.xml` (your backend project root).
Do not overwrite your entire repository. Back up or commit current changes first.

## Run
PowerShell:
```powershell
$env:LLM_API_KEY="your-key"  # optional for RULE mode; do not commit
mvn spring-boot:run
```
If your project has a Maven wrapper, use `./mvnw.cmd spring-boot:run` instead.

## Fast demo (PowerShell)
```powershell
$base = "http://localhost:8080/api"
$p = Invoke-RestMethod -Method Post -Uri "$base/products" -ContentType "application/json" -Body '{"sku":"DEMO-001","name":"Demo Headphones","category":"ELECTRONICS","currentPrice":100.00,"stockLevel":12,"reorderThreshold":10,"demandVelocity":1.0}'
$id = $p.id
Invoke-RestMethod -Method Post -Uri "$base/products/$id/orders" -ContentType "application/json" -Body '{"quantity":5}'
Start-Sleep -Seconds 3
$pricing = Invoke-RestMethod "$base/pricing-suggestions?status=PENDING"
$reorders = Invoke-RestMethod "$base/reorder-suggestions?status=PENDING"
$pricing | ConvertTo-Json
$reorders | ConvertTo-Json
Invoke-RestMethod -Method Patch -Uri "$base/pricing-suggestions/$($pricing[0].id)" -ContentType "application/json" -Body '{"status":"ACCEPTED"}'
Invoke-RestMethod -Method Patch -Uri "$base/reorder-suggestions/$($reorders[0].id)" -ContentType "application/json" -Body '{"status":"ACCEPTED"}'
Invoke-RestMethod "$base/products"
```
Use a fresh SKU if you rerun. Existing H2 data persists. For AI, first set LLM_API_KEY and test:
```powershell
Invoke-RestMethod -Method Post -Uri "$base/advisor/mode" -ContentType "application/json" -Body '{"mode":"AI"}'
Invoke-RestMethod "$base/advisor/mode"
```
Then create another low-stock event. AI failures fall back to rules. A fallback does not prove the live API worked; check logs.

## Known constraints
- No UI included.
- In-memory mode defaults to RULE on restart.
- Demand-spike baseline is the mean current velocity across the product's category; with only one product, a 3x spike cannot trigger. Seed multiple same-category products to demonstrate.
- In-process async events are not crash durable.
- No end-to-end compile or live Gemini API verification was performed when preparing this patch.
