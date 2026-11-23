# Use of AI Tools - Lab 5, Problem 1 (TDD on Adapter + Facade)

**Tool:** Claude Opus 5.5 (Anthropic), via claude.ai, with access to the project folder and read-only access
to the team GitHub repository.

**Prompts** (translated from Romanian):
1. *(with the Lab05 sheet, the EMF guide for the X-ray analysis component and the link to Federica's pull request #8)*
   "I need to do my part of the tests: show that they fail, then the implementation, after I take screenshots
   and confirm that they fail, to demonstrate the whole process. I want something similar to Federica's
   pull request, but don't open a pull request and don't push to the branch, I will do that myself."
2. Follow-ups after each step, once the screenshot was taken and the commit was made:
   "it worked, let's go on" (step 3), "it worked" (step 5), "let's do it" (iteration 2, step 1),
   "done" (iteration 2, step 3), "done, I pushed" (iteration 2, step 5).

**How the result was used:**
- The AI wrote the tests first (steps 1-2), then the code (steps 3-4), then the refactoring (step 5),
  one step at a time; the next step was only written after I confirmed the result of the previous one.
- I ran every step in IntelliJ, checked that the tests failed or passed as expected, took the screenshots
  and made one commit per group of steps (`test:`, `feat:`, `refactor:`), then pushed the branch myself.
- The `pom.xml` is the same file as on Federica's branch, so the two branches merge without conflicts.
- The documentation follows the structure of Federica's TDD documentation (`docs/state-observer/tdd`).

**Advantages:** the TDD steps stay clearly separated in the commit history; the tests use small test doubles,
so they run without the real PDF / DICOM libraries and without the AI model.
**Disadvantages:** the AI cannot run IntelliJ, so every step had to be checked and documented manually;
the tests were written against the existing Lab 4 classes, so some small additions (getters,
`Detection.setHeatmap()`) were needed in the teammates' classes.


# Use of AI Tools - Labs 6-7 (working pipeline and AOP)

**Tool:** Claude Opus 5.5 (Anthropic), via claude.ai, with read-only access to the team GitHub repository.

**Prompts** (translated from Romanian):
1. *(with the Lab06 sheet)* "How should we split this between 4 people?" and then "we have not presented for
   2-3 weeks and I want real progress", which led to the split: the ML model (Ianis), the Spring Boot backend
   (Federica), the analysis API and infrastructure (Alexandra), the Flutter app and the X-ray analysis
   pipeline with its aspect (Teodora).
2. *(with the Lab 7 sheet)* "Do my part, look at Federica's branch to see what she already added, work in
   several commits, tell me when to push, and rebase on what is now on main."

**How the result was used:**
- The AI rebased the branch on `main`, adapted the facade to Ianis's `ModelRegistry`, then wrote the tests first
  (`test:` commits) and the code after them (`feat:` commits) for the preprocessing, the heatmaps and the aspect.
- The AspectJ setup in `pom.xml` copies the one on Federica's and Alexandra's branches.
- I ran `mvn test` in IntelliJ (the aspect is only woven by Maven), checked the build log for the woven join
  points and pushed the branch myself.

**Advantages:** the aspect is small and reusable, and the pipeline now works on real images, so the backend can
call it directly. **Disadvantages:** the AI could not run Maven, so the woven aspect test had to be checked locally;
the heatmap is box-based until the model exposes its activations.
