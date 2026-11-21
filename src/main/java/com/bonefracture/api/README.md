# api package

Skeleton for Alexandra's analysis API (empty placeholders, no implementation).

These types stay plain and empty until Federica adds the Spring Boot skeleton.
After that:

- `AnalysisController` becomes a Spring `@RestController` wiring the endpoints in
  `docs/api/api-contract.md` to `FractureAnalysisFacade` and
  `ModelComparisonService`.
- the `dto` classes get their fields (and any JSON annotations).
- `LoggingAspect` and `ExceptionHandlingAspect` are applied across the
  controllers.

Nothing here should be implemented before the Spring Boot backend exists, to
avoid duplicating Federica's work.
