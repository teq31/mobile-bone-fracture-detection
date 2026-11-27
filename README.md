# mobile-bone-fracture-detection
Mobile application for bone fracture detection and localization in X-ray images, with visual explanations, for orthopedic students.

- **Backend:** Spring Boot (Java 21), H2 or PostgreSQL, AspectJ aspects, YOLOv8 model with ONNX Runtime
- **App:** Flutter (Android, web), in `mobile/`
- **Docs:** `docs/` (one folder per component), `demo/` (X-rays for the demo)

---

## How to run everything locally

You need two terminals: one for the backend, one for the app. The commands below are for **Windows PowerShell**;
the Mac / Linux version is right under each one.

### What you need once

| Tool | Version | Check with |
|---|---|---|
| JDK | 21 or newer | `java -version` |
| Maven | 3.9+ (or use `mvnw.cmd` / `./mvnw` from the repo) | `mvn -v` |
| Flutter | stable | `flutter --version` |
| Android Studio | with an emulator (Device Manager) | `flutter doctor` must show Flutter and Android toolchain with a check mark |

`flutter doctor --android-licenses` accepts the Android licences (answer `y` to all).

### 1. Get the code

```powershell
git clone https://github.com/teq31/mobile-bone-fracture-detection.git
cd mobile-bone-fracture-detection
```

Already cloned: `git checkout main` and `git pull`.

### 2. Download the model (once)

The model file (43 MB) is not in git, it is in the GitHub Release `model-v1`:

```powershell
curl.exe -L --create-dirs -o models/yolov8-fracture.onnx https://github.com/teq31/mobile-bone-fracture-detection/releases/download/model-v1/yolov8-fracture.onnx
```
Mac / Linux: the same command with `curl` instead of `curl.exe`.

Without it the backend still starts, but the analysis finds nothing (placeholder model).

### 3. Start the backend (terminal 1)

```powershell
$env:ADMIN_EMAIL="admin@clinic.com"; $env:ADMIN_PASSWORD="admin12345"
mvn spring-boot:run
```
Mac / Linux: `ADMIN_EMAIL=admin@clinic.com ADMIN_PASSWORD=admin12345 mvn spring-boot:run`

Wait for `Started BoneFractureApplication` and leave the terminal open (closing it stops the server).
`ADMIN_EMAIL` / `ADMIN_PASSWORD` create the administrator account at startup; there is no other way to get one.

**Port 8080 already in use?** (Oracle XE, Tomcat, another backend...) Use 8081 and remember it for step 4:

```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

Check it works: open `http://localhost:8080/actuator/health` (or `8081`) in the browser, it shows `{"status":"UP"}`.

The database is H2 **in memory**: accounts and analyses disappear when the backend stops.
While it runs, the tables can be seen at `http://localhost:8080/h2-console`
(JDBC URL `jdbc:h2:mem:bonefracture`, user `sa`, no password).

### 4. Start the app (terminal 2)

1. Start the emulator: Android Studio -> More Actions -> **Virtual Device Manager** -> play button.
   Wait until the Android home screen shows.
2. Check that Flutter sees it and note its id (e.g. `emulator-5554`):
   ```powershell
   flutter devices
   ```
3. Run the app **on the emulator** (`-d`, otherwise Flutter may pick Chrome, where the backend is not reachable):
   ```powershell
   cd mobile
   flutter pub get
   flutter run -d emulator-5554 --dart-define=API_BASE_URL=http://10.0.2.2:8080
   ```
   `10.0.2.2` is your computer as seen from the emulator. Use `:8081` if the backend runs on 8081.

The first build takes a few minutes. In the `flutter run` terminal: `r` hot reload, `R` restart, `q` quit.

**Real phone instead of the emulator:** phone and computer on the same Wi-Fi, USB debugging on, then
`flutter run --dart-define=API_BASE_URL=http://<computer IP>:8080` (`ipconfig` shows the IP).

**Without the backend model:** `flutter run -d emulator-5554 --dart-define=USE_MOCK_ANALYSIS=true` simulates the analysis
on the phone (accounts, appointments and notifications still need the backend).

### 5. Try the whole flow

X-rays with a visible fracture are in `demo/xrays/`; drag one onto the emulator window and it lands in Downloads
(choose it with **Gallery**, or **PDF / DICOM** -> Downloads).

1. **Create account -> Doctor**, then log out.
2. Log in as **admin** (`admin@clinic.com` / `admin12345`) -> **Doctors** -> **Verify** the doctor. Log out.
3. **Create account -> Patient** -> **Analyze** -> `demo/xrays/1-right-front.png` -> Wrist -> Analyze.
   Expected: *fracture ~87%* with a box near the bottom of the wrist (all expected results: `demo/README.md`).
   Turn the heatmap on / off; the analysis is in **History**.
4. On the result: **Ask a doctor for an appointment** -> pick the doctor -> Send.
5. Log in as the doctor -> **Appointments** -> **Confirm** (a verified doctor can also **Compare all models**).
6. Back as the patient -> **Notifications**: the appointment is confirmed.
7. As admin: **Audit** (who did what) and **Speed** (how long each analysis step takes).

---

## Tests

```powershell
mvn clean test          # backend, all tests (use clean after switching branches)
cd mobile
flutter analyze
flutter test
```

The same checks run on GitHub for every pull request (`.github/workflows/`).

## Docker (backend + PostgreSQL)

```powershell
copy .env.example .env      # then edit the passwords in .env
docker compose up --build
```
The image downloads the model by itself. The backend is on `http://localhost:8080` (`BACKEND_PORT` in `.env`).

---

## Common problems

| Problem | Fix |
|---|---|
| `Port 8080 was already in use` | something else uses 8080 (`netstat -ano \| findstr :8080`, then `tasklist /FI "PID eq <pid>"`). Start the backend on 8081 (step 3) and run the app with `:8081`. |
| The app says it can't reach the server | backend still running? App started on the emulator (`-d emulator-5554`), not Chrome? Port in `API_BASE_URL` the same as the backend's? In the emulator's Chrome, `http://10.0.2.2:8080/actuator/health` must show `UP`. |
| `flutter run` offers only Chrome / Edge | the emulator is not started yet: start it and run `flutter devices` again. |
| `Installed Build Tools revision 36.0.0 is corrupted` | delete `<Android SDK>\build-tools\36.0.0` and install it again from Android Studio -> SDK Manager -> SDK Tools -> Show Package Details -> Build-Tools 36.0.0. Then `flutter clean`. |
| `flutter` / `mvn` not recognized | the tool is not in PATH: add its `bin` folder to PATH and reopen the terminal. |
| Tests from another branch fail | old compiled classes: run `mvn clean test`, not `mvn test`. |
| Login as admin fails | the backend was started without `ADMIN_EMAIL` / `ADMIN_PASSWORD` in the same terminal. |
| The emulator window is off-screen | click it and press Windows + arrow key; or set `window.x`, `window.y`, `window.scale` in `<avd folder>\<name>.avd\emulator-user.ini`. |

## More documentation

| Topic | Where |
|---|---|
| Backend endpoints | `docs/api/backend-api.md`, `docs/api/api-contract.md` |
| Model, mAP, ONNX | `docs/detection-model/README.md`, `ml/README.md` |
| Demo X-rays and expected results | `demo/README.md` |
| Mobile app | `mobile/README.md` |
| Design patterns, TDD, AOP per component | `docs/facade-adapter`, `docs/state-observer`, `docs/caching`, `docs/accounts-audit`, `docs/backend`, `docs/tdd-user-detection` |
