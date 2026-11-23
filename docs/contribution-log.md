# KPFL Project: Contribution Log

Group members: Brandon Bell, Theo-Jean Forester, Landan Williams, Lamar Martin
Repository: https://github.com/brandonbell2357-wq/Kingston-Premier-Football-League-KPFL-
Where this file lives: `docs/contribution-log.md`

**How to use this log**

- Each member writes their own row or entry before the weekly demonstration. Do not write for someone else.
- Commit this file every week so the repository history shows the dates.
- Tag the repository at each checkpoint (`week1`, `week2`, `week3`, `week4`, `week5`).
- Record all AI-assisted work and outside sources, as the course policy requires.

---

## Week 1: Requirements and system design

**Dates worked:** TBD
**Checkpoint deliverable:** requirements, use cases, UML class diagram, interface sketch, storage design, test plan

### Group summary

- Drafted the Week 1 design documents with these sections: project overview and plan, requirements, actors and business rules, eight use cases, UML class diagram (three views), design justification, interface design, persistence and validation strategy, and a 47-case initial test plan.
- Chose a Java console application with sequential text files for league data and random access files for products and tickets.
- Proposed rules D1 to D4 and eight questions for the lecturer (see the open questions in the Week 1 design document).

### Per-member entries

| Member | Role | What I did | Hours | Evidence (commits, pull requests, files) |
| --- | --- | --- | --- | --- |
| Brandon Bell | Project and integration lead | Set up the repository folders, reviewed the requirements document, Created the UML class diagram and contributed to finding requirements| 3.5| [Week 1 requirements PDF](week1/KPFL_Week_1_Requirements_and_Design.pdf) |
| Theo-Jean Forester | TBD (roles not yet assigned) | | | |
| Landan Williams | TBD (roles not yet assigned) | | | |
| Lamar Martin| Fixtures & Stadiums Lead |Audited the 28-case initial test plan matrix across normal, boundary and invalid scenarios. Verified all 13 OOP relationship justifications (inheritance, aggregation, composition, association) against domain model requirements. | 3 |  [Week 1 requirements PDF](week1/KPFL_Week_1_Requirements_and_Design.pdf) |

### Decisions made

- Tickets belong to the fixture, not the stadium, because capacity is counted per fixture.
- Shop prices treated as GCT-inclusive (tax is 15/115 of the total), pending the lecturer's answer.
- Standings are rebuilt from completed fixtures rather than stored.

### Problems and how they were solved

- TBD

### Plan for next week

| Member | Task |
| --- | --- |
| Brandon Bell | |
| Theo-Jean Forester | |
| Landan Williams | |
| Lamar Martin| Implementation of staduim, footballteam entity in java|

### AI assistance and sources

- The first draft of the Week 1 design document was produced with Claude (Anthropic). The group reviewed it, and every member must be able to explain the design and the parts credited to them.
- Members: add what you changed or rewrote, and any other sources used.

---

## Week 2: Core implementation

**Dates worked:** Oct 29 - Current
**Checkpoint deliverable:** Person hierarchy, teams, personnel, stadiums, first unit tests, console prototype

### Per-member entries

| Member | What I did | Hours | Evidence |
| --- | --- | --- | --- |
| Brandon Bell |Created person class and its derived classes. Reviewed exceptions and fixing errors within the menu class.| 4| [Person refactor](https://github.com/brandonbell2357-wq/Kingston-Premier-Football-League-KPFL-/commit/fc2a358), [MatchOfficial update](https://github.com/brandonbell2357-wq/Kingston-Premier-Football-League-KPFL-/commit/9f9adfb), [Menu](https://github.com/brandonbell2357-wq/Kingston-Premier-Football-League-KPFL-/commit/95b8c33), [PR #8](https://github.com/brandonbell2357-wq/Kingston-Premier-Football-League-KPFL-/pull/8), [PR #11](https://github.com/brandonbell2357-wq/Kingston-Premier-Football-League-KPFL-/pull/11) |
| Theo-Jean Forester | | | |
| Landan Williams | Creation Of custom exception classes and Implementing then on existing classes | 4| |
| Lamar Martin | Developed the Stadium and FootballTeam domain entity classes (field encapsulation and input validation) with AI assistance: Gemini drafted the code, and I checked, changed and tested it. The files were uploaded to the repository by Brandon Bell. Following an AI-assisted code review with (Claude), made and committed 4 fixes on branch fix/review-issues and raised issue #9 for the remaining cleanup, assigned to the authors. | 4.1 + (2) | src/model/Stadium.java, src/model/FootballTeam.java (uploaded by Brandon Bell),[pull request #10](https://github.com/brandonbell2357-wq/Kingston-Premier-Football-League-KPFL-/pull/10), [issue #9](https://github.com/brandonbell2357-wq/Kingston-Premier-Football-League-KPFL-/issues/9) |

### Decisions made
- Encapsulated `FootballTeam` status initialization to default to 'Pending' upon creation.
- Defined `Stadium` capacity and ticket price validation in model constructors to prevent invalid values before service execution.
- `Menu` is the only console menu; `Main` just starts it.
- A team's squad size must be at least 1.
- A person's ID can be set after creation through `setId()`, with the same blank/null validation as the constructor.
- Style fixes in other members' code are raised as an issue for the author instead of being changed directly.

### Problems and how they were solved
Problem 0: Preventing empty or invalid string inputs for registration numbers and stadium names.
Solution: Added trim and null/blank checks in class constructors throwing \`IllegalArgumentException\`.

Problem 1: Two different console menus. Main.java had its own menu (options 1–5, exit on 5), while Menu.java had a different, more complete menu (options 1–4, exit on 0, with submenus) that was never called. 
Solution: Rewrote Main.java so it only prints the title banner and calls new Menu().run(). Menu is now the single console menu. The demo Stadium/FootballTeam creation in Main was removed.

Problem 2: Program crashed when input ended. When the input stream ended (Ctrl+D / Ctrl+Z, or piped input ran out), scanner.nextLine() threw NoSuchElementException and the program stopped with an error. 
Solution: In Menu.readInt(), check scanner.hasNextLine() before reading. If there is no more input, return 0, which means "Back" in a submenu and "Save and Exit" in the main menu, so the program closes cleanly.

Problem 3: Default constructors created people with an ID that could never change. Person() sets the ID to "000", and id was declared final, so an object made with a default constructor (for example new Player()) could never get a real ID. Two default players on the same team clashed with "A player with ID 000 is already on this team". 
Solution: Removed final from Person.id and added a setId() setter that rejects null or blank IDs, matching the validation in the constructor. Also corrected the comments in Spectator and LeagueCommissioner, which wrongly said the default constructor uses "empty text" (it actually sets ID "000" and name/gender "N/A").

Problem 4: A team could be created with a squad size of 0. FootballTeam.setSquadSize() only rejected negative numbers, so a team with squad size 0 was accepted, and then every addPlayer() call failed with "The squad is full (maximum 0 players)". 
Solution: Changed the check in setSquadSize() to squadSize < 1, with the message "Squad size must be at least 1".

### Remaining issues
Remaining cleanup raised as issue #9 (not fixed yet): a personal VS Code workspace file committed in src/exception, a split package line in UnderageException.java, a stray /// line in FootballTeam.java, and wrong indentation in Coach.java. Assigned to Brandon Bell and Landan Williams, with step-by-step instructions in the issue.


### Plan for next week

| Member | Task |
| --- | --- |
| Brandon Bell | |
| Theo-Jean Forester | |
| Landan Williams |  |
| Lamar Martin | Implement the Fixture class and match result recording (points for win/draw/loss), add any fixture-related exceptions, and collaborate on standings logic |

### AI assistance and sources
Used Gemini Notebook to draft the code for Stadium.java and FootballTeam.java, including the input validation logic. Lamar Martin checked changed and tested the code; it was uploaded to the repository by Brandon Bell.

Used Claude (Anthropic) to review and compile the Week 2 code to explain a few of the issues found. Claude explained and how to fix; Lamar Martin made every edit, opened the pull request and raised issue #9.

---

## Week 3: League operations

**Dates worked:** TBD
**Checkpoint deliverable:** fixtures, officials, results, standings, custom exceptions

### Per-member entries

| Member | What I did | Hours | Evidence |
| --- | --- | --- | --- |
| Brandon Bell | Created the three Week 3 business-rule exceptions (`TeamNotEligibleException`, `SchedulingConflictException`, `FixtureStateException`). Merged the fixtures work with `main`, added `FootballLeague` (lists of teams, stadiums, officials and fixtures; BR2 team clash and stadium clash checks; result recording and standings), connected the team, squad, stadium, official, fixture, result and standings menu options so each exception shows a clear message, added sample data for demonstrations, and wrote `LeagueTest` (60 checks). Reviewed, compiled and ran all three test files (171 checks). | 3 | [exceptions commit](https://github.com/brandonbell2357-wq/Kingston-Premier-Football-League-KPFL-/commit/5b13c3f), [pull request #12](https://github.com/brandonbell2357-wq/Kingston-Premier-Football-League-KPFL-/pull/12), [FootballLeague, menu and tests](https://github.com/brandonbell2357-wq/Kingston-Premier-Football-League-KPFL-/commit/0746444), [pull request #16](https://github.com/brandonbell2357-wq/Kingston-Premier-Football-League-KPFL-/pull/16), `src/model/FootballLeague.java`, `test/LeagueTest.java` |
| Theo-Jean Forester | | | |
| Landan Williams |Create the StandingsService(), MatchResult() & standingsEntry() classes| 3 | |
| Lamar | | | |

### Decisions made

- `FootballLeague` holds the lists and enforces rules that need more than one fixture (BR2 and stadium clashes); `Fixture` enforces BR1, BR3 and BR8 itself (Brandon).
- Only an identical date and time counts as a clash; the same day at another time is allowed, as in the test plan (Brandon).
- Standings are rebuilt from completed fixtures and are not stored (Brandon).
- Sample data includes a pending and a suspended team so BR1 can be demonstrated from the menu (Brandon).

### Problems and how they were solved

- The fixtures branch was behind `main`, so merging it directly would have undone later changes. `main` was merged into it first, then the code was compiled and all tests were run (Brandon).
- Nothing held the lists of teams, fixtures and officials, so BR2 could not be checked. `FootballLeague` was added to do this (Brandon).

### Plan for next week

| Member | Task |
| --- | --- |
| Brandon Bell | Search facilities and reports (`ReportService`) and their menu options |
| Theo-Jean Forester | |
| Landan Williams | |
| Lamar | |

### AI assistance and sources

- Brandon Bell: `FootballLeague`, the menu hookup, the sample data and `LeagueTest` were drafted with Claude (Anthropic). I reviewed the code, merged `main` into the fixtures branch, compiled the whole project, ran the tests, made my own fixes and committed it, and I can explain how each rule is enforced. The three exceptions follow the pattern of the existing exception classes.

---

## Week 4: Match-day services and persistence

**Dates worked:** TBD
**Checkpoint deliverable:** tickets, shop, tax, receipts, saving and loading, search, three reports

### Per-member entries

| Member | What I did | Hours | Evidence |
| --- | --- | --- | --- |
| Brandon Bell | Added `ReportService` with search for teams, players, stadiums and fixtures and three reports (league standings, team directory, stadium report), and connected them to the Reports menu. Cleaned the service code (removed an unused field) and merged the work through pull request #16. | 3 | [ReportService](https://github.com/brandonbell2357-wq/Kingston-Premier-Football-League-KPFL-/commit/6a6d1f0), [service refactor](https://github.com/brandonbell2357-wq/Kingston-Premier-Football-League-KPFL-/commit/2c1ab4a), [menu search and reports](https://github.com/brandonbell2357-wq/Kingston-Premier-Football-League-KPFL-/commit/5c8d538), [pull request #16](https://github.com/brandonbell2357-wq/Kingston-Premier-Football-League-KPFL-/pull/16), `src/service/ReportService.java` |
| Theo-Jean Forester | | | |
| Landan Williams | | | |
| Lamar | | | |

### Decisions made

- Search and reports sit in a separate `ReportService` so the menu only asks for results and prints them (Brandon).
- Reports are built from the league's data rather than stored (Brandon).

### Problems and how they were solved

- A constructor change that skipped validation made 9 of the 68 `ModelTest` checks fail. The tests caught it, and the fix is to call the setters again so the rules apply (Brandon).

### Plan for next week

| Member | Task |
| --- | --- |
| Brandon Bell | Integrate all modules, update the UML to match the code, final testing and the demonstration |
| Theo-Jean Forester | |
| Landan Williams | |
| Lamar | |

### AI assistance and sources

- Brandon Bell: `ReportService` and the menu changes were drafted with an AI coding assistant (GitHub Copilot in VS Code). I reviewed the code, compiled and ran the project and the tests, and edited it before committing. I can explain how each search and report works.

---

## Week 5: Integration, testing and defense

**Dates worked:** TBD
**Checkpoint deliverable:** full system, test report, user guide, technical report, reflections, demonstration

### Per-member entries

| Member | What I did | Hours | Evidence |
| --- | --- | --- | --- |
| Brandon Bell | | | |
| Theo-Jean Forester | | | |
| Landan Williams | | | |
| Lamar | | | |

### Decisions made

- TBD

### Problems and how they were solved

- TBD

### AI assistance and sources

- TBD

---

## Individual reflection notes

Each member keeps short notes here during the project and turns them into the final reflection.

| Member | Week | What I learned or found hard |
| --- | --- | --- |
| Brandon Bell | | |
| Theo-Jean Forester | | |
| Landan Williams | | |
| Lamar | | |
