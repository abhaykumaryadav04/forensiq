# ForensiQ Frontend

A production-style React frontend for the ForensiQ identity document screening backend (Spring Boot). Built with React, Vite, Tailwind CSS, Axios and React Router. It only ever displays data returned by the real backend — no mock AI results, no client-side risk calculation.

## Getting started

```bash
npm install
cp .env.example .env   # then edit VITE_API_BASE_URL if needed
npm run dev
```

The app runs at `http://localhost:5173` and talks directly to `VITE_API_BASE_URL` (default `http://localhost:8080`).

## Important things to know before you run this against your backend

1. **HTTP Basic auth is required.** `SecurityConfig` in `identity-scanning` requires a valid username/password on every route except `/actuator/health`. Open **Settings** in the app and enter the credentials configured on the backend via `app.security.username` / `app.security.password`. Credentials are kept only in `sessionStorage` for the current tab (never `localStorage`), and are attached as an `Authorization: Basic` header on every request.

2. **CORS is not configured on the backend.** No `CorsConfig`/`@CrossOrigin` was found in the uploaded source. If you run this frontend on a different origin than the backend (which is the normal case for `npm run dev` at `:5173` against a backend at `:8080`), the browser will block the requests until you add CORS support on the backend (or serve both from the same origin in production). This frontend does not modify your Spring Boot code, per your requirements — you'll need to add that yourself.

3. **No history / audit listing endpoints exist yet.** The backend only exposes:
   - `POST /api/v1/screenigs` — create a screening request
   - `POST /api/v1/screenigs/{requestId}/camera` — submit an image and run the full pipeline
   - `GET /api/v1/reports/{documentId}` — download the PDF report
   - `GET /actuator/health` — health check

   There is no endpoint to list past scans or the full audit ledger, and no endpoint to re-fetch a past result by document ID. **Scan History**, **Audit**, and result re-viewing after a page refresh are therefore backed by a client-side session log (real API responses only, kept in `sessionStorage` for the current tab) rather than invented data, and the UI says so explicitly. Add list endpoints on the backend (e.g. `GET /api/v1/screenigs` and `GET /api/v1/audit`) to replace this with real server-side history.

4. **`componentRiskScores` isn't currently returned.** `CentralRiskResult.componentRiskScores` exists on the backend but `FinalVerdictMapper` doesn't copy it into `FinalVerdictResponse`. The Component Risk Breakdown section is wired up and will render automatically the moment the backend starts including it — until then it stays hidden rather than showing fabricated numbers.

## Project structure

```
src/
  components/
    Layout/            Sidebar, Header (backend status indicator)
    CameraScanner/      Webcam capture, guide frame, stability-based auto-capture
    ProcessingStepper/   10-step pipeline progress display
    Verdict/            VerdictCard, RiskScore
    Verification/       Verification Evidence cards (OCR, MRZ, QR, cross-field, forensics, AI, field tampering)
    ExtractedFields/    Extracted information table
    Tampering/          Tampering analysis section
    RiskBreakdown/      Component risk score bars
    Report/             PDF report download button
    Audit/               Audit hash-chain trail
    common/              Card, StatusBadge, EmptyState, ErrorState, Spinner
  pages/                 Dashboard, NewScan, ScanResult, ScanHistory, Reports, Audit, Settings
  services/              api.js (axios instance + Basic auth + error mapping), screeningApi.js, reportApi.js
  context/                AuthContext (Basic auth credentials), ScanHistoryContext (session scan log)
  hooks/                  useBackendStatus, useApiAuthSync, useSimulatedProgress
  constants/              verdicts.js, documentTypes.js, extractedFields.js, processingSteps.js
```

## Camera capture flow

1. Requests camera permission and starts the webcam stream (rear camera preferred on mobile).
2. Shows a guide frame and samples a small grayscale region of the video every 150ms, comparing it to the previous sample.
3. When the frame-to-frame difference stays low for about a second, it auto-captures. A manual shutter button and file-upload fallback are always available, and auto-capture can be toggled off.
4. The captured image is shown for confirmation (Retake / Scan Document) before it's sent to the backend.
5. On submit, a screening request is created (`POST /api/v1/screenigs`), then the image is submitted for full screening (`POST /api/v1/screenigs/{requestId}/camera`). The processing stepper animates through the documented pipeline stages while that single request is in flight (the backend doesn't emit incremental progress events), then the real response drives the result page.

## Notes on the backend contract

The DTOs referenced throughout this codebase (`FinalVerdictResponse`, `CameraScanResponse`, `VarificationProcessingResult`, `TamperingDetectionResult`, `ExtractionResponse`, etc.) were taken directly from the Spring Boot source you provided, not from the "possible response" example in the spec — some field names (e.g. `confodence`, `crossfieldCheck`, `mrzValidatinResult`) preserve the backend's actual (misspelled) property names so the JSON deserializes correctly without backend changes.
