# Kingston Premier Football League (KPFL)

The Kingston Premier Football League (KPFL) requires a group of three to five members to create an object-oriented solution to manage their football competition. The KPFL consists of football teams, players, coaches, match officials, stadiums, fixtures, ticket sales, merchandise, and match-day operations.

This repository holds the group's six-week object-oriented programming project.

## Group members

| Member | Role |
| --- | --- |
| Brandon Bell | To be assigned |
| Theo-Jean Forester | To be assigned |
| Landan Williams | To be assigned |
| Lamar Martin| To be assigned |

## Repository layout

| Folder | Purpose |
| --- | --- |
| `src/model` | Domain classes such as `Person`, `FootballTeam`, `Fixture` and `Product` |
| `src/exception` | Custom exceptions for the business rules |
| `src/service` | League and shop logic that enforces the business rules |
| `src/storage` | Saving and loading data from files |
| `src/ui` | The console menu |
| `test` | Unit tests |
| `data/sample` | Sample data files used for demonstrations and tests |
| `docs` | Design documents, the UML diagram and the contribution log |

## Working agreements

- Each member commits under their own name.
- One branch per feature, merged by pull request after another member reviews it.
- Each member updates their own entry in `docs/contribution-log.md` every week.
- The repository is tagged at each weekly checkpoint (`week1` to `week5`).
- AI-assisted work and outside sources are recorded in the contribution log.

## Documents

- `docs/week1/KPFL_Week_1_Requirements_and_Design.pdf`: the Week 1 requirements and design document (requirements, actors and rules, use cases, design decisions, persistence, test plan, UML diagram and console menu).
- `docs/kpfl-uml-week1.drawio`: UML class diagram (open it at app.diagrams.net).
- `docs/contribution-log.md`: weekly record of what each member did.

## How to compile and run

The program is not written yet. These instructions will be completed in Week 2, once the console prototype has a `main` method. The expected steps are:

1. Install a JDK (Java 17 or later).
2. Compile all sources into an `out` folder.
3. Run the main class from `out`.
