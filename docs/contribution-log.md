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
| Brandon Bell | TBD (roles not yet assigned) | Set up the repository folders, reviewed the requirements document, Created the UML class diagram and contributed to finding requirements| 3.5| [Week 1 requirements PDF](week1/KPFL_Week_1_Requirements_and_Design.pdf) |
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
| Brandon Bell |Created person class and its derived classes. Created a few exceptions| 4| |
| Theo-Jean Forester | | | |
| Landan Williams | Creation Of custom exception classes and Implementing then on existing classes | 4| |
| Lamar Martin | Developed the Stadium and FootballTeam domain entity classes (field encapsulation and input validation) with AI assistance: Gemini drafted the code, and I checked, changed and tested it. The files were uploaded to the repository by Brandon Bell. Following an AI-assisted code review with (Claude), made and committed 4 fixes on branch fix/review-issues and raised issue #9 for the remaining cleanup, assigned to the authors. | 4.1 + (2) | src/model/Stadium.java, src/model/FootballTeam.java (uploaded by Brandon Bell), pull request: (paste link), issue #9 |

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
| Brandon Bell | | | |
| Theo-Jean Forester | | | |
| Landan Williams | | | |
| Lamar | | | |

### Decisions made

- TBD

### Problems and how they were solved

- TBD

### Plan for next week

| Member | Task |
| --- | --- |
| Brandon Bell | |
| Theo-Jean Forester | |
| Landan Williams | |
| Lamar | |

### AI assistance and sources

- TBD

---

## Week 4: Match-day services and persistence

**Dates worked:** TBD
**Checkpoint deliverable:** tickets, shop, tax, receipts, saving and loading, search, three reports

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

### Plan for next week

| Member | Task |
| --- | --- |
| Brandon Bell | |
| Theo-Jean Forester | |
| Landan Williams | |
| Lamar | |

### AI assistance and sources

- TBD

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
