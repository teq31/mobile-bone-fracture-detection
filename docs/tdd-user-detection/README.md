# TDD: user registration and fracture detection

Part of Problem 1, Lab 5. Two TDD iterations (red, green, refactor) on top of the
Factory Method and Strategy classes from Lab 4.

## Service (SOA)
`user.UserService` is the contract other parts of the app use for accounts:
`register(role, name, email, password)` and `login(email, password)`.
`UserServiceImpl` picks the right `UserFactory` for the role (Factory Method)
and keeps the registered users by email.

## Iteration 1: user registration
| Step | Commit | Result |
|---|---|---|
| Red | `test: add failing tests for user registration and UserService` | 8 tests, 5 failing |
| Green | `feat: hash password on sign up and implement UserService` | 8 tests passing |
| Refactor | `refactor: move password hashing to PasswordHasher` | 8 tests passing |

Tested behaviour (`UserFactoryTest`, `UserServiceTest`):
- each factory creates the right user type (Student, Doctor, Patient, Administrator)
- the password is not stored in plain text (SHA-256 hash) and login checks it
- a new doctor is not verified
- the service creates the user for the given role, rejects an email used twice
  and checks the credentials at login

## Iteration 2: fracture detection
| Step | Commit | Result |
|---|---|---|
| Red | `test: add failing tests for FractureDetector and ModelRegistry` | 9 new tests, 7 failing |
| Green | `feat: main model lookup and null checks in FractureDetector` | 17 tests passing |
| Refactor | `refactor: single null check in FractureDetector` | 17 tests passing |

Tested behaviour (`FractureDetectorTest`, `ModelRegistryTest`):
- `FractureDetector` analyzes with the current model and `setModel()` switches it (Strategy)
- every result is saved on the X-ray
- a null model is rejected
- `ModelRegistry.getMainModel()` returns the model marked as main and throws
  `IllegalStateException` when there is none
- a detector created from the registry starts with the main model

The two tests that passed in the red step (`keepsAllModels`, `resultIsSavedOnTheXRay`)
cover behaviour that already existed from Lab 4.

## Running the tests
```
mvn test
```
Needs Java 17+ and Maven. JUnit 5 is declared in `pom.xml`.
