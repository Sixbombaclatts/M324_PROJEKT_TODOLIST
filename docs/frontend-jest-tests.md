# Frontend Jest Tests

## Repository

https://github.com/Sixbombaclatts/M324_PROJEKT_TODOLIST.git

## Branch

feature/tests-frontend

## Testziel

Für das React-Frontend wurden zwei Jest-Tests mit React Testing Library erstellt.

## Tests

1. `shows no tasks when backend returns an empty list`

Prüft, ob die App korrekt startet, wenn das Backend eine leere Todo-Liste zurückgibt.

2. `shows tasks returned by the backend`

Prüft, ob zwei Tasks, die vom Backend geliefert werden, im Frontend angezeigt werden.

## Ausführen der Tests

```powershell
cd frontend
npm test -- --runInBand
```