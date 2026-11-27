# Mobile app (Flutter)

Teodora's part. Android (and web) app for the whole flow: account -> X-ray -> fracture boxes -> history ->
appointment with a doctor -> notification. Since lab 8 it uses the real model on the server.

| Screen | What it does | Backend |
|---|---|---|
| Login / Create account | patient, doctor or student; the session is kept | `/api/auth/*` (Federica) |
| Analyze | X-ray from the camera, the gallery or a PDF / DICOM file, body part (wrist, elbow, ankle) | `POST /api/analyses` (Alexandra), YOLOv8 ONNX model (Ianis) |
| Result | the X-ray with a box and confidence per finding, heatmap on / off, warning for patients and students; "Compare all models" for verified doctors; "Ask a doctor" for patients | `GET /api/analyses/{id}`, `/{id}/image`, `/{id}/comparison` |
| History | past analyses, opened again with their result | `GET /api/analyses/history` (stored in the database) |
| Appointments | the patient picks a verified doctor from a list, the doctor confirms or sends a reminder, both can cancel | `GET /api/doctors`, `/api/appointments` (Federica, State) |
| Notifications | appointment confirmed / cancelled / reminder, mark as read | `/api/notifications` (NotificationAspect) |
| Admin: Doctors | doctors waiting for verification, "Verify" | `/api/admin/doctors/*` (Ianis) |
| Admin: Audit | who registered, logged in or verified a doctor | `GET /api/admin/audit` (AuditLoggingAspect) |
| Admin: Speed | how long each analysis step takes, slowest first | `GET /api/performance/stats` (PerformanceMonitoringAspect) |

An administrator account is created when the backend starts with `ADMIN_EMAIL` and `ADMIN_PASSWORD` set;
logging in with it opens the three admin tabs instead of the patient / doctor ones.

## Run it

1. Backend with the model: see `docs/detection-model/README.md` (download the ONNX file once), then
   `mvn spring-boot:run` or `docker compose up`.
2. App:

```
cd mobile
flutter pub get
flutter run                                                     # Android emulator: backend at 10.0.2.2:8080
flutter run --dart-define=API_BASE_URL=http://192.168.1.20:8080  # real phone on the same Wi-Fi (the computer's IP)
flutter run --dart-define=USE_MOCK_ANALYSIS=true                # demo analysis on the phone, no model needed
```

`android/app/src/main/AndroidManifest.xml` already allows HTTP to the backend (`usesCleartextTraffic`), needed while
the backend runs without HTTPS.

The boxes are in the pixels of the uploaded X-ray, so the app draws them over the image from `imageUrl`.
For a PDF or DICOM upload the server sends the original file back, which the app can't draw: the result
screen then shows the findings without the picture.

## CI

`.github/workflows/mobile-ci.yml`: `flutter analyze` and `flutter test` on every pull request that touches
`mobile/`. On `main` the release APK is built and can be downloaded from the run; a `v*` tag attaches it to a
GitHub Release. Set the repository variable `API_BASE_URL` to the deployed backend so the APK talks to it.

## Structure

```
lib/
  main.dart            app, theme, restores the session (admin -> AdminHomeScreen)
  app_scope.dart       services + logged-in user, shared by all screens
  config.dart          API_BASE_URL, USE_MOCK_ANALYSIS (--dart-define)
  models/              User, Analysis / Detection / Comparison, Doctor, Appointment, AppNotification, AuditEntry, OperationStats
  services/            ApiClient (JWT, ProblemDetail errors), Auth, Analysis (Http + Mock), Appointment, Notification, Admin
  screens/             login, register, home (4 tabs), analyze, result, comparison, history, appointments, notifications, admin
  widgets/             XRayViewer (boxes + heatmap), ImageFit, common widgets
test/                  JSON of every endpoint, box position on the screen, start screen
```

## Still open

- PDF and DICOM: the adapters are ready, but `PdfDocument` / `DicomFile` still need the real libraries
  (PDFBox, dcm4che) on the server, and the server should return the converted PNG as `imageUrl`.
- CORS on the backend, only for the web version.
