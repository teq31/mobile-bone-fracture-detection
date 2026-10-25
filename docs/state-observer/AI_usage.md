# Use of AI Tools - Lab 4, Problem 1 (State + Observer)

**Tool:** Claude Opus 5.5 (Anthropic), via claude.ai (prompts 1-2) and Claude Code (prompt 3).

**Prompts:**
1. *(with the Lab04 sheet, the Project Vision document and the team class diagram available)* "My part of Problem 1 is the State and Observer design patterns, in Java. Write only the basic classes: attributes, method declarations and relationships, without implementation. Use the State pattern for the appointment lifecycle (requested, confirmed, cancelled) and the Observer pattern for notifications (new comment, appointment requested / confirmed / reminder, analysis finished), consistent with the class diagram."
2. "Add a class diagram of these classes for the presentation."
3. *(Claude Code, with the team repository)* "Analyse my teammate's branch (structure and approach), tell me if my approach is correct and which files I should remove; then restructure my files to match and push them on my own branch."

**How the result was used:** the class names and enums (Appointment, AppointmentStatus, Notification, NotificationType) were aligned with the team class diagram. The AI generated temporary User, Doctor and Patient stubs; they were removed and the code now uses the teammates' classes from the `user` package (branch `ianis/design-patterns`). Following prompt 3, the sources were moved to `src/main/java` to match the team's project structure, and the documentation was moved to `docs/state-observer`. The code was compiled together with the teammates' classes and committed to the team GitHub repository.

**Advantages:** quick, consistent skeleton; the two patterns are connected (state changes notify the observers).
**Disadvantages:** the AI does not see the teammates' code, so package names and the User classes had to be aligned manually.
