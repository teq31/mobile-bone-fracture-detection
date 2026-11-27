# Backend API: accounts, appointments, notifications

Spring Boot backend (Federica). The analysis endpoints are described in `api-contract.md` (Alexandra).

- Base URL (local): `http://localhost:8080` (start with `mvn spring-boot:run`; use another port with
  `mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"`)
- Bodies are JSON. Dates use ISO-8601 local time, e.g. `"2026-12-01T10:00:00"`.
- Every endpoint except register and login needs the token from login:
  `Authorization: Bearer <token>`
- Errors use the standard Spring `ProblemDetail` body:

```json
{ "type": "about:blank", "title": "Conflict", "status": 409, "detail": "Appointment is already confirmed", "instance": "/api/appointments/3/confirm" }
```

| Status | Meaning |
|---|---|
| 400 | invalid data (missing field, bad email, password shorter than 8, chosen user is not a doctor) |
| 401 | missing / invalid / expired token, or wrong email or password at login |
| 403 | logged in, but not allowed (wrong role, or not your appointment / notification) |
| 404 | appointment or notification does not exist |
| 409 | email already used, or the action is not allowed in the appointment's current state |

---

## Accounts

### POST /api/auth/register (public)

```json
{ "name": "John Doe", "email": "john@mail.com", "password": "secret123", "role": "PATIENT" }
```

`role`: `PATIENT`, `DOCTOR` or `STUDENT` (`ADMINISTRATOR` is rejected). A new doctor has `verified: false`.

`201 Created`:

```json
{ "id": 1, "name": "John Doe", "email": "john@mail.com", "role": "PATIENT", "verified": false }
```

### POST /api/auth/login (public)

```json
{ "email": "john@mail.com", "password": "secret123" }
```

`200 OK` (the token is valid for 24 hours, `expiresIn` is in seconds):

```json
{ "token": "eyJhbGciOiJIUzI1NiJ9...", "tokenType": "Bearer", "expiresIn": 86400 }
```

The token holds the user id (`sub`), `email` and `role`.

### GET /api/auth/me

`200 OK`: the same body as register, for the logged-in user.

---

## Doctors

### GET /api/doctors

Any logged-in user. The doctors verified by an administrator, sorted by name: the list a patient
chooses from when requesting an appointment (its `id` is the `doctorId` below). Doctors who are
not verified yet are not in the list.

`200 OK`:

```json
[
  { "id": 4, "name": "Andrei Ionescu", "email": "andrei@clinic.com" },
  { "id": 2, "name": "Maria Pop", "email": "maria@clinic.com" }
]
```

---

## Appointments

Appointment body returned by every endpoint below:

```json
{ "id": 3, "doctorId": 2, "doctorName": "Dr. Smith", "patientId": 1, "patientName": "John Doe",
  "dateTime": "2026-12-01T10:00:00", "status": "REQUESTED" }
```

`status`: `REQUESTED` -> `CONFIRMED` -> `CANCELLED` (rules of the State pattern):

| Current status | confirm | cancel | reminder |
|---|---|---|---|
| REQUESTED | -> CONFIRMED | -> CANCELLED | 409 |
| CONFIRMED | 409 | -> CANCELLED | stays CONFIRMED |
| CANCELLED | 409 | 409 | 409 |

| Endpoint | Who | Result |
|---|---|---|
| `POST /api/appointments` body `{ "doctorId": 2, "dateTime": "2026-12-01T10:00:00" }` | patient | `201`, status `REQUESTED`; `dateTime` must be in the future |
| `GET /api/appointments` | any user | `200`, list ordered by date: a patient gets theirs, a doctor gets theirs |
| `GET /api/appointments/{id}` | the patient or doctor of the appointment | `200` |
| `POST /api/appointments/{id}/confirm` | the doctor of the appointment | `200` |
| `POST /api/appointments/{id}/cancel` | the patient or the doctor | `200` |
| `POST /api/appointments/{id}/reminder` | the doctor | `200` |

---

## Notifications

Created automatically by `NotificationAspect` (AOP) when an appointment is confirmed, cancelled
or a reminder is sent; they go to the patient.

```json
{ "id": 5, "type": "APPOINTMENT_CONFIRMED", "message": "Your appointment on 2026-12-01T10:00 was confirmed",
  "createdAt": "2026-11-20T17:05:12.345", "read": false }
```

`type`: `APPOINTMENT_CONFIRMED`, `APPOINTMENT_CANCELLED`, `APPOINTMENT_REMINDER`.

| Endpoint | Who | Result |
|---|---|---|
| `GET /api/notifications` | any user | `200`, the user's notifications, newest first |
| `POST /api/notifications/{id}/read` | the recipient | `200`, the notification with `read: true` |

---

## Health check (public)

Spring Boot Actuator. No token needed; only the status is shown, no details.

| Endpoint | Result |
|---|---|
| `GET /actuator/health` | `200 { "status": "UP" }` when the backend and the database work, `503 { "status": "DOWN" }` otherwise |
| `GET /actuator/health/liveness` | the application is running (for Docker / Azure restarts) |
| `GET /actuator/health/readiness` | the application can receive requests |

The other Actuator endpoints (`/actuator/env`, `/actuator/beans`, ...) are not exposed.

---

## Running with PostgreSQL (docker-compose)

The backend uses H2 in memory by default. In `docker-compose.yml` set these variables on the backend
service; Spring Boot uses them instead of H2:

```
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/<DB_NAME>
SPRING_DATASOURCE_USERNAME=<DB_USERNAME>
SPRING_DATASOURCE_PASSWORD=<DB_PASSWORD>
JWT_SECRET=<at least 32 random characters>
```

`mvn clean package` builds `target/mobile-bone-fracture-detection-0.1.0.jar` (the Dockerfile runs it with Java 21).
