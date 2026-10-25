# State + Observer: appointments and notifications

Part of Problem 1, Lab 4 (Mobile Bone Fracture Detection). Java, declarations only:
attributes, method signatures and relationships, no functionality
(method bodies only throw UnsupportedOperationException).

## State: appointment lifecycle
| Role in the pattern | Class |
|---|---|
| Context | `appointment.Appointment` |
| State interface | `appointment.state.AppointmentState` |
| Concrete states | `RequestedState`, `ConfirmedState`, `CancelledState` |

`Appointment.confirm()`, `cancel()` and `sendReminder()` delegate to the current state.
Each state decides what is allowed and which state comes next:
- Requested: confirm -> Confirmed, cancel -> Cancelled, no reminder
- Confirmed: cancel -> Cancelled, reminder allowed
- Cancelled: final, nothing allowed

`AppointmentStatus` (REQUESTED, CONFIRMED, CANCELLED) is the enum from the team class diagram,
returned by `getStatus()`.

## Observer: notifications
| Role in the pattern | Class |
|---|---|
| Subject interface | `notification.observer.NotificationSubject` |
| Base subject (keeps the observers) | `AbstractNotificationSubject` |
| Concrete subject | `Appointment` |
| Observer interface | `NotificationObserver` |
| Concrete observers | `PushNotificationObserver` (Firebase), `EmailNotificationObserver` (SendGrid), `NotificationHistoryObserver` (in-app list) |

`Notification` + `NotificationType` come from the team class diagram
(NEW_COMMENT, APPOINTMENT_REQUESTED, APPOINTMENT_CONFIRMED, APPOINTMENT_REMINDER, ANALYSIS_FINISHED).

## How the two patterns work together
`Appointment` extends `AbstractNotificationSubject`: every state change
(for example Requested -> Confirmed) notifies the observers with the matching `NotificationType`.
Other classes that produce events (`Comment` -> NEW_COMMENT, `AnalysisResult` -> ANALYSIS_FINISHED)
can extend `AbstractNotificationSubject` too.

## Notes
- Sources are in `src/main/java/com/bonefracture/` (`appointment` and `notification` packages).
- `User`, `Doctor`, `Patient` are the team classes from the `user` package (branch `ianis/design-patterns`, Factory Method).
- `java.util.Observer` is not used because it is deprecated since Java 9.
