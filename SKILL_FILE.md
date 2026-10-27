---
name: echipa-conventii-cod
description: Ghid de conventii de cod, stil si colaborare pentru echipa de 4 membri. Foloseste acest skill cand echipa are nevoie de reguli clare pentru nume de clase, metode, commit-uri, PR-uri si organizarea proiectului.
---

# Ghid de Echipa si Conventii de Cod

Acest skill defineste standardele de codare, naming conventions si regulile de colaborare pentru o echipa de 4 membri, conceput special in limba romana (fara diacritice) pentru a putea fi usor inteles, mentinut si modificat (inclusiv de catre asistenti AI precum Claude).

## 1. Conventii de Naming

Regulile de mai jos se aplica universal in cadrul proiectului, indiferent de limbajul de programare principal (adaptat contextual):

*   **Clase si Structuri (Classes / Interfaces / Structs):** `PascalCase` (ex: `UserAccount`, `OrderController`, `PaymentProcessor`). Numele trebuie sa fie substantive clare.
*   **Metode si Functii (Methods / Functions):** `camelCase` (ex: `calculateTotalAmount`, `getUserById`, `validateInput`). Numele trebuie sa inceapa cu un verb care descrie actiunea.
*   **Variabile si Proprietati (Variables / Fields):** `camelCase` (ex: `userEmail`, `maxRetryCount`, `isActive`). Evita abrevierile obscure (foloseste `currentIndex` in loc de `idx`).
*   **Constante (Constants):** `UPPER_SNAKE_CASE` (ex: `MAX_CONNECTION_TIMEOUT`, `DEFAULT_PAGE_SIZE`).
*   **Fisiere si Module (Files & Folders):** 
    *   Pentru cod sursa: foloseste conventia specifica tehnologiei (ex: `kebab-case` pentru componente web/TS/JS: `user-profile.component.ts`, sau `snake_case` / `PascalCase` dupa caz).
    *   Pentru documentatie: `kebab-case.md`.

---

## 2. Organizarea Echipei si Structura Proiectului (4 Membri)

Pentru o echipa de 4 dezvoltatori, recomandam o impartire clara a responsabilitatilor pe module sau pe straturi arhitecturale (Layers) pentru a evita conflictele de merge (merge conflicts):

*   **Arhitectura pe straturi / module:**
    1.  **Arhitectura / Core / Config:** Setarea initiala, utilitare globale, gestionarea erorilor si configurarea de baza.
    2.  **Modulul 1 (Feature A - Backend / Servicii principale):** Logica de business si fluxurile principale de date.
    3.  **Modulul 2 (Feature B - Interfata / UI / Client):** Componentele vizuale, ecranele sau API-urile de consum.
    4.  **Testare si Integrare (QA & CI/CD):** Scriere de teste unitare/integrare, revizuire cod (Code Review) si pipeline-uri.

---

## 3. Workflow de Colaborare si Git Strategy

Pentru a lucra curat fara a ne suprascrie munca:

1.  **Branching Strategy (Git Flow simplificat):**
    *   `main` sau `master`: Codul stabil, gata de productie/prezentare.
    *   `develop` (optional) sau branch-uri de feature directe din `main`: `feature/<nume-scurt-task>` (ex: `feature/auth-login`, `feature/user-dashboard`).
2.  **Commit Messages (in limba engleza sau romana, recomandat convențional):**
    *   `feat: adaugare ecran de autentificare`
    *   `fix: rezolvare eroare la calculul TVA`
    *   `refactor: curatare cod duplicat in servicii`
    *   `docs: actualizare ghid instalare`
3.  **Pull Requests (PR) & Code Review:**
    *   Orice cod nou intra in branch-ul principal prin **Pull Request**.
    *   Este nevoie de **cel putin 1 aprobare (approval)** de la un alt membru al echipei inainte de merge.
    *   Nu se face merge peste cod care nu compileaza sau pica testele.