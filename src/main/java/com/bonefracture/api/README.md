# api package (Alexandra's analysis endpoints)

AnalysisController exposes:

- `POST /api/analyses` - run the main-model detection on an uploaded image.
- `GET /api/analyses/history` - past analyses for the current user.
- `GET /api/analyses/{id}/comparison` - run every model and return the
  per-finding agreement (ModelComparisonService).

Bean wiring for the analysis stack lives in `api/config/AnalysisBeansConfig`.

Depends on teammates' work that is still stubbed:

- `FractureAnalysisFacade.analyzeXRay` / `getHistory` and the detection models'
  `detect(...)` are Teodora's / Ianis's TODOs. Until those are done, the
  detection and history responses come back empty. The endpoints, wiring,
  DTOs and comparison flow are complete on this side.
