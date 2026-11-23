# Use of AI Tools - Lab 7 (mobile app)

**Tool:** Claude Opus 5.5 (Anthropic), via claude.ai, with read-only access to the team GitHub repository.

**Prompts** (translated from Romanian):
1. *(with the Lab06 sheet)* "How should we split this between 4 people?", then "we have not presented for 2-3 weeks
   and I want real progress". The answer proposed the target demo (log in -> photograph or upload a wrist X-ray ->
   see the box on the fracture -> the analysis appears in the history), the split of the work and the JSON of
   `POST /api/analyses`.
2. *(with the Lab 7 sheet)* "Do my part, look at Federica's branch to see what she already added, work in several
   commits and tell me when to push."

**How the result was used:**
- The AI read Federica's `docs/api/backend-api.md` and Alexandra's `docs/api/api-contract.md` on their branches and
  wrote the app against exactly that JSON (models, services), then the screens, the tests and this documentation,
  one commit per part.
- The AI could not install Flutter, so I generated the platform folders with `flutter create`, ran
  `flutter analyze`, `flutter test` and the app on the emulator, and fixed what was needed before pushing.
- The analysis is simulated on the phone (`MockAnalysisService`, shown as "Demo analysis") until the backend
  endpoint and the trained model exist; switching to the real endpoint is one `--dart-define`.

**Advantages:** the app works now with the real accounts, appointments and notifications, and the analysis part
needs no change when the endpoint is ready. **Disadvantages:** the code was written without a compiler, so it had to
be checked locally; the box-based heatmap is drawn on the phone instead of using `heatmapUrl`.
