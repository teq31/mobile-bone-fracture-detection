# Backend: accounts, appointments and notifications (Lab 6)

Federica's part of Lab 6, Problem 1: the Spring Boot backend that the rest of the project builds on,
with a database, REST endpoints, SOA services and an aspect (AOP).
The endpoints, with request and response examples, are in [docs/api/backend-api.md](../api/backend-api.md).

## Run it

```
mvn spring-boot:run                     # http://localhost:8080
mvn test                                # all tests (H2 in memory, no Docker needed)
mvn clean package                       # target/mobile-bone-fracture-detection-0.1.0.jar
```

If port 8080 is taken: `mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"`.

- Java 21, Spring Boot 3.5.16 (web, validation, data-jpa, security, oauth2-resource-server).
- Database: H2 in memory by default. With docker-compose, `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`
  and `SPRING_DATASOURCE_PASSWORD` switch it to PostgreSQL (driver included). Set `JWT_SECRET` too.
- AOP: AspectJ, woven at compile time by `aspectj-maven-plugin` (same setup as the other aspects in the team).

## Architecture

```
Flutter app
   |  HTTP + JWT
   v
api (controllers)          AuthController, AppointmentController, NotificationController, ApiExceptionHandler
   |
   v
services (SOA)             AuthService, UserService (DatabaseUserService), AppointmentBookingService,
   |                       NotificationInboxService
   |-- domain (patterns)   appointment (STATE + OBSERVER subject), user (FACTORY METHOD), notification (OBSERVER)
   |-- aspect (AOP)        NotificationAspect
   v
persistence (JPA)          UserEntity, AppointmentEntity, NotificationEntity + repositories
```

| Package | Contents |
|---|---|
| `api` | REST controllers and request / response records (`api.dto`); `ApiExceptionHandler` turns service exceptions into 400 / 401 / 403 / 404 / 409 |
| `security` | `SecurityConfig` (public: register, login; everything else needs a JWT), `TokenService` (creates the JWT) |
| `auth` | `AuthService` + `AuthServiceImpl` (API rules, JWT), `DatabaseUserService` (implementation of `user.UserService` that saves to the database) |
| `booking` | `AppointmentBookingService`, `NotificationInboxService` + implementations, `DatabaseNotificationObserver` |
| `persistence` | JPA entities (tables `users`, `appointments`, `notifications`) and Spring Data repositories |
| `aspect` | `NotificationAspect` |

The domain classes with the design patterns (from Labs 4-5) are not JPA entities: the `persistence`
package describes the tables, and the services map between the two. This way the pattern classes and their
tests stay unchanged.

## How the design patterns are used in the backend

**Factory Method (Ianis, `user` package).** `DatabaseUserService` is a second implementation of the
`UserService` interface. At register, the factory for the role creates the right type of user
(`DoctorFactory` creates a `Doctor` that is not verified yet), then the account is saved with a BCrypt
password. The in-memory `UserServiceImpl` and its tests are unchanged.

**State (`appointment` package).** The backend does not repeat the rules. For confirm / cancel / reminder,
`AppointmentBookingServiceImpl` loads the appointment, rebuilds the domain `Appointment` in its saved state
(`Appointment.restore`, `AppointmentState.forStatus`), calls `AppointmentService`, and the state classes decide:
the new status is saved, or `IllegalStateException` becomes `409 Conflict`.

**Observer (`notification` package).** `DatabaseNotificationObserver` is a new concrete observer that saves
every notification for the app. The booking service attaches it to the appointment.

## AOP: NotificationAspect

Sending a notification is a cross-cutting concern: it is not part of the rules of an appointment.
In Lab 5 the state classes created the notifications themselves; now they only change the state, and
`NotificationAspect` sends the notification:

| Pointcut (method execution) | Advice | Notification |
|---|---|---|
| `Appointment.confirm()` | `@AfterReturning` | `APPOINTMENT_CONFIRMED` |
| `Appointment.cancel()` | `@AfterReturning` | `APPOINTMENT_CANCELLED` |
| `Appointment.sendReminder()` | `@AfterReturning` | `APPOINTMENT_REMINDER` |

- `@AfterReturning` runs only if the method finished without an exception, so an action that is not
  allowed in the current state sends nothing.
- The aspect sends the notification to the appointment's observers (Observer pattern): the aspect decides
  *when* and *what*, the observers decide *where* it goes (database, push, e-mail).
- The build log shows where the aspect was woven ("Join point ... advised by afterReturning advice from
  NotificationAspect").
- The 6 notification tests from Lab 5 (`AppointmentNotificationTest`) pass unchanged after the notification
  code was moved from the states into the aspect.

## Decisions

| Decision | Why |
|---|---|
| AspectJ compile-time weaving, not Spring AOP | same setup as the team's other aspects; it also works on classes that are not Spring beans, such as the domain `Appointment` |
| `<parameters>true</parameters>` for the AspectJ compiler | without it Spring cannot read `@PathVariable` names and every `/{id}` endpoint returns 400 |
| BCrypt for passwords | slow on purpose and salted automatically, so stolen hashes are hard to crack (SHA-256 is fast and, without salt, equal passwords give equal hashes) |
| JWT with the user id, email and role | the backend keeps no sessions; the Flutter app sends the token with every request |
| Same error message for a wrong email and a wrong password | nobody can find out which emails have accounts |
| Administrators cannot self-register | an administrator verifies doctors, so the role must not be public |

## Tests

73 tests, all passing (`mvn test`). New in Lab 6:

| Test class | What it checks |
|---|---|
| `BoneFractureApplicationTest` | the application starts |
| `PersistenceTest` | users, appointments and notifications are saved and read back; unique email |
| `AuthControllerTest` | register (hashed password, unverified doctor, duplicate email, invalid data), login, token required |
| `DatabaseUserServiceTest` | the factories create the right type of user; saved with role and hashed password; login |
| `AppointmentControllerTest` | State rules through the API (409), who may do what (403), not found (404) |
| `NotificationControllerTest` | every change creates the right notification for the patient; none when the action fails; mark as read |
| `AppointmentRestoreTest` | an appointment loaded from the database continues from its saved state |

## Not done yet

- No endpoint for an administrator to verify doctors (doctors can receive appointments while unverified).
- The doctor gets no notification when a patient requests an appointment (`APPOINTMENT_REQUESTED`).
- Not yet run against a real PostgreSQL database (only H2); the docker-compose setup is Alexandra's part.
