# Accounts: doctor verification and audit (AOP)

Part of Problem 1, labs 6-7. Builds on `UserService` (Factory Method, lab 4; TDD, lab 5),
which the backend implements with the database in `DatabaseUserService`.

## Service (SOA)
`UserService` now has three operations: `register`, `login` and `verifyDoctor(doctorEmail)`.
A new doctor is not verified (`DoctorFactory`); an administrator verifies the account.
Verifying an unknown email throws `UserNotFoundException` (404), verifying someone who is not a
doctor throws `IllegalArgumentException` (400).

Administrators cannot register through the API, so the first one is created at startup from
`ADMIN_EMAIL` / `ADMIN_PASSWORD` (`AdminAccountInitializer`). Without them no administrator exists.

## Endpoints
All three need an administrator token (`@RequiresRole(Role.ADMINISTRATOR)`); other roles get 403.

| Method | Path | Body | Result |
|---|---|---|---|
| GET | `/api/admin/doctors/unverified` | | doctors waiting for verification |
| POST | `/api/admin/doctors/verify` | `{"email": "dan@clinic.com"}` | the verified doctor |
| GET | `/api/admin/audit` | | the last account actions, newest first |

## AuditLoggingAspect (AOP)
`audit.AuditLoggingAspect` records who did what with the accounts, so `UserService` and its
implementations contain no audit code. It is woven at compile time (aspectj-maven-plugin) around:

| Join point | Recorded as | Actor | Target |
|---|---|---|---|
| `UserService+.register(..)` | `REGISTER` | the new user's email | the role |
| `UserService+.login(..)` | `LOGIN` | the email | the email; success = the login result |
| `UserService+.verifyDoctor(..)` | `VERIFY_DOCTOR` | the logged-in administrator (email from the JWT) | the doctor's email |

Each entry has the time and whether the action worked; a failed action is recorded and its exception
is still thrown. Passwords are never recorded. The last 1000 entries are kept in `AuditLog`
(shared with the admin endpoint through `AuditConfig`) and every entry is also written to the
`audit` logger.

Example after registering a doctor and a patient and verifying the doctor:
```
VERIFY_DOCTOR  admin@clinic.com  dan@clinic.com  ok
LOGIN          ion@mail.com      ion@mail.com    failed
LOGIN          ion@mail.com      ion@mail.com    ok
LOGIN          admin@clinic.com  admin@clinic.com ok
REGISTER       ion@mail.com      PATIENT         ok
REGISTER       dan@clinic.com    DOCTOR          ok
REGISTER       admin@clinic.com  ADMINISTRATOR   ok
```

## Tests
- `user/UserServiceTest`, `auth/DatabaseUserServiceTest`: verifying a doctor, only doctors, unknown email
- `api/AdminControllerTest`: the three endpoints, 403 for other roles, 404 and 400
- `auth/AdminAccountInitializerTest`: administrator created from the settings, nothing without them
- `audit/AuditLoggingAspectTest`: what is recorded for each action, failures, no passwords, order
