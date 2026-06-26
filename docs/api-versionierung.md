# REST-API-Versionierung — Dokumentation

**Projekt:** M324 ToDo-Liste (Spring Boot + React)  
**Modul:** 324 — DevOps-Prozesse mit Tools unterstützen  
**Gewählte Methode:** Query-Parameter-Versionierung (`?api=v1` / `?api=v2`)

---

## 1. Einleitung

Eine REST-API ist selten statisch. Neue Anforderungen führen zu Änderungen an Endpoints, Request-Bodies oder Response-Formaten. Wenn bestehende Clients (z. B. ein React-Frontend, Mobile Apps oder externe Dienste) weiterhin funktionieren sollen, während neue Features eingeführt werden, braucht man ein **Versionierungskonzept**.

**API-Versionierung** bedeutet, mehrere Varianten derselben Schnittstelle parallel anzubieten. Ein Client kann explizit angeben, welche Version er nutzen möchte. So lassen sich **Breaking Changes** (Änderungen, die alte Clients unbrauchbar machen) kontrolliert einführen, ohne alle Nutzer gleichzeitig migrieren zu müssen.

In unserem Projekt wurde die bestehende ToDo-REST-API um zwei Versionen erweitert:

| Version | Aufruf | Beschreibung |
|---------|--------|--------------|
| **v1** | `?api=v1` | Ursprüngliches Format — flache Task-Liste mit Feld `taskdescription` |
| **v2** | `?api=v2` | Neues Format — umschlossene Antwort mit `description`, Metadaten und JSON-Aktionsantworten |

Das React-Frontend nutzt **v1**, damit die App weiterhin wie gewohnt funktioniert. **v2** dient als erweiterte Schnittstelle und ist per curl/Browser demonstrierbar.

---

## 2. Übersicht der Versionierungsmethoden

Es gibt vier gängige Ansätze, eine REST-API zu versionieren:

### 2.1 URI-Pfad-Versionierung

Die Version ist Teil der URL:

```
GET /api/v1/tasks
GET /api/v2/tasks
```

### 2.2 Header-Versionierung

Die URL bleibt gleich, die Version wird per HTTP-Header übergeben:

```
GET /tasks
Header: API-Version: 2
```

### 2.3 Query-Parameter-Versionierung *(gewählt)*

Die Version steht als Parameter in der URL:

```
GET /tasks?api=v1
GET /tasks?api=v2
```

### 2.4 Content Negotiation (Media Type)

Die Version ist im `Accept`-Header als Media Type codiert:

```
GET /tasks
Accept: application/vnd.company.tasks+json;version=2
```

---

## 3. Vor- und Nachteile der Methoden

### 3.1 URI-Pfad-Versionierung

| Vorteile | Nachteile |
|----------|-----------|
| Sehr einfach und sofort erkennbar | URL ändert sich bei neuer Version |
| Gut für Dokumentation, Logs und Tests | REST-Puristen: URL soll Ressource, nicht Version beschreiben |
| Breite Unterstützung in Frameworks und Tools | Jeder Endpoint-Pfad muss angepasst werden |
| Standard in vielen öffentlichen APIs (GitHub, Stripe) | |

### 3.2 Header-Versionierung

| Vorteile | Nachteile |
|----------|-----------|
| Saubere, stabile URLs | Version nicht direkt im Browser sichtbar |
| Flexibel für viele Clients auf derselben URL | Header leicht vergessen oder falsch setzen |
| Trennung von Ressource und Version | Schwerer zu testen ohne spezielle Tools |
| | Swagger/OpenAPI-Darstellung komplizierter |

### 3.3 Query-Parameter-Versionierung *(unsere Wahl)*

| Vorteile | Nachteile |
|----------|-----------|
| URL-Pfad bleibt stabil (`/tasks`) | Weniger verbreitet als URI-Pfad in grossen APIs |
| Einfach im Browser und mit curl testbar | Parameter kann vergessen werden |
| Schnelle Implementierung in Spring Boot mit `@RequestParam` | Wirkt weniger „enterprise“ als `/v1/` |
| Kein spezieller Header nötig | Caching kann Query-Strings anders behandeln |
| Default-Wert möglich (Abwärtskompatibilität) | |

### 3.4 Content Negotiation

| Vorteile | Nachteile |
|----------|-----------|
| Sehr REST-konform (HTTP-Standard) | Komplex und schwer lesbar |
| Gleiche URL, unterschiedliche Repräsentation | Schlechte Tool-Unterstützung (Swagger, Postman) |
| Feingranulare Steuerung | Für Schul-/Projektumgebungen oft Overkill |

---

## 4. Begründung der gewählten Methode

Wir haben uns für **Query-Parameter-Versionierung** mit dem Parameter `api` entschieden.

**Gründe:**

1. **Einfachheit:** In Spring Boot lässt sich die Version mit `@RequestParam(name = "api", defaultValue = "v1")` direkt in jedem Controller-Endpoint einbinden — ohne zusätzliche Router, Interceptors oder URL-Umstrukturierung.

2. **Stabile URLs:** Die bestehenden Pfade (`/tasks`, `/delete`, `/update`) bleiben erhalten. Nur der Query-Parameter unterscheidet die Versionen. Das minimiert Änderungen am Frontend.

3. **Demonstrierbarkeit:** Beim Lehrer-Vortrag kann man im Browser oder mit curl sofort den Unterschied zeigen:
   - `http://localhost:8080/tasks?api=v1` → flache JSON-Liste
   - `http://localhost:8080/tasks?api=v2` → umschlossenes Objekt mit `count` und `items`

4. **Abwärtskompatibilität:** Fehlt der Parameter, wird automatisch `v1` verwendet (`defaultValue = "v1"`). Bestehende Aufrufe ohne Parameter funktionieren weiter.

5. **Testbarkeit:** Unit-Tests können mit MockMvc einfach `.param("api", "v1")` bzw. `.param("api", "v2")` setzen — ohne Header-Konfiguration.

6. **Flexibilität für v2:** In v2 konnten wir das Response-Format sinnvoll erweitern (Wrapper-Objekt, Feld `description` statt `taskdescription`, JSON-Aktionsantworten bei POST), ohne v1 zu brechen.

**Abgewogen gegen URI-Pfad:** URI wäre der Industriestandard, hätte aber mehr Refactoring erfordert (alle Pfade, Frontend, Tests). Für den Projektumfang und die Anforderung „funktionierende App + zwei Versionen demonstrieren“ ist der Query-Parameter der effizientere Weg.

---

## 5. Schritt-für-Schritt-Implementierung

### Schritt 1: Versions-Enum erstellen

Datei: `backend/src/main/java/com/example/demo/ApiVersion.java`

- Enum mit `V1("v1")` und `V2("v2")`
- Statische Methode `from(String)` parst den Parameter und wirft bei ungültigen Werten eine `InvalidApiVersionException`

### Schritt 2: Fehlerbehandlung

Dateien:
- `InvalidApiVersionException.java` — Exception für unbekannte Versionen
- `ApiVersionExceptionHandler.java` — `@RestControllerAdvice`, antwortet mit HTTP 400

Beispiel: `GET /tasks?api=v99` → `400 Bad Request` mit Meldung „Unsupported API version: v99“

### Schritt 3: v2-Datenmodelle (DTOs)

Dateien:
- `TaskV2Dto.java` — Task mit Feld `description` (statt `taskdescription`)
- `TaskV2ListResponse.java` — Wrapper: `{ apiVersion, count, items[] }`

Diese Klassen machen den Unterschied zwischen v1 und v2 für die Demo sichtbar.

### Schritt 4: TaskController anpassen

Datei: `TaskController.java`

Jeder Endpoint erhält den Parameter:

```java
@RequestParam(name = "api", defaultValue = "v1") String apiVersion
```

**Verhalten pro Endpoint:**

| Endpoint | v1 | v2 |
|----------|----|----|
| `GET /` und `GET /tasks` | Flache `List<Task>` | `TaskV2ListResponse` mit `count` und `items` |
| `POST /tasks` | Task-Objekt (201 Created) | `TaskV2Dto` (201 Created) |
| `POST /delete` | `"redirect:/"` | JSON `{ apiVersion, action, description }` |
| `POST /update` | `"redirect:/"` | JSON `{ apiVersion, action, description }` |
| `POST /toggle-done` | `"redirect:/"` | JSON `{ apiVersion, action, description, done }` |

Intern nutzen beide Versionen dieselbe `TaskRepository`-Logik — nur das Response-Format unterscheidet sich.

### Schritt 5: Frontend anpassen

Datei: `frontend/src/App.jsx`

```javascript
const API_BASE = 'http://localhost:8080'
const API_VERSION = 'v1'
const apiUrl = (path) => `${API_BASE}${path}?api=${API_VERSION}`
```

Alle `fetch()`-Aufrufe nutzen `apiUrl('/tasks')`, `apiUrl('/delete')` usw. Die App arbeitet damit ausschliesslich mit v1.

### Schritt 6: Unit-Tests

Datei: `backend/src/test/java/com/example/demo/TaskControllerTest.java`

Tests für **v1:**
- Leere Liste abrufen
- Tasks erstellen und abrufen
- Default auf v1 wenn Parameter fehlt

Tests für **v2:**
- Wrapped List mit `apiVersion`, `count`, `items[].description`
- Task erstellen → Response mit `description`-Feld
- Delete und Toggle → JSON-Aktionsantwort

Test für **ungültige Version:**
- `?api=v99` → HTTP 400

Frontend-Test (`App.test.jsx`): prüft, dass `fetch` mit `?api=v1` aufgerufen wird.

### Schritt 7: CI-Verifikation

GitHub Actions Workflows:
- **Java CI with Maven** (`maven.yml`): `mvn -B package` im `backend/`-Ordner
- **Node.js CI Frontend** (`node.js.yml`): `npm test` und `npm run build` im `frontend/`-Ordner

Beide Pipelines müssen grün sein — lokal verifiziert mit `mvn test` und `npm test`.

---

## 6. Demo-Anleitung (Vorführung beim Lehrer)

### App starten

```bash
# Terminal 1 — Backend
cd backend
./mvnw spring-boot:run

# Terminal 2 — Frontend
cd frontend
npm run dev
```

Frontend: http://localhost:5173 — Tasks anlegen, bearbeiten, löschen, erledigen.

### Versionen vergleichen (curl oder Browser)

```bash
# v1 — flache Liste
curl "http://localhost:8080/tasks?api=v1"

# v2 — umschlossene Antwort
curl "http://localhost:8080/tasks?api=v2"

# Task erstellen (v2)
curl -X POST "http://localhost:8080/tasks?api=v2" \
  -H "Content-Type: application/json" \
  -d "{\"taskdescription\":\"Demo Task\"}"

# Ungültige Version
curl "http://localhost:8080/tasks?api=v99"
```

**Erwartete v1-Antwort (GET):**
```json
[
  { "id": 1, "taskdescription": "Demo Task", "dueDate": "", "reminderEnabled": false, "done": false }
]
```

**Erwartete v2-Antwort (GET):**
```json
{
  "apiVersion": "v2",
  "count": 1,
  "items": [
    { "id": 1, "description": "Demo Task", "dueDate": "", "reminderEnabled": false, "done": false }
  ]
}
```

---

## 7. Zusammenfassung und Fazit

Wir haben erfolgreich **Query-Parameter-Versionierung** in der ToDo-REST-API implementiert. Die App funktioniert über **v1** unverändert, während **v2** ein erweitertes Response-Format mit Metadaten und konsistenten JSON-Aktionsantworten bietet.

**Erreichte Ziele:**
- Recherche und Bewertung von vier Versionierungsmethoden
- Begründete Wahl der Query-Parameter-Methode
- Implementierung in Spring Boot mit sauberer Fehlerbehandlung
- Unit-Tests für beide Versionen und ungültige Eingaben
- Frontend-Kompatibilität durch v1-Nutzung
- CI-Pipeline (Maven + Node.js) läuft durch

**Lessons Learned:**
- Versionierung sollte früh geplant werden, nicht erst bei Breaking Changes
- Ein sichtbarer Unterschied zwischen v1 und v2 (Feldnamen, Response-Struktur) erleichtert Demo und Tests
- Default-Werte (`defaultValue = "v1"`) sichern Abwärtskompatibilität
- Zentrale Hilfsfunktionen im Frontend (`apiUrl()`) vereinfachen spätere Migration auf v2

**Mögliche Erweiterungen:**
- Frontend-Umschalter v1/v2 für Vergleich in der UI
- Deprecation-Warnung in v1-Responses (`"deprecated": true`)
- Swagger/OpenAPI-Dokumentation mit beiden Versionen

---

## Anhang: Geänderte und neue Dateien

| Datei | Aktion |
|-------|--------|
| `ApiVersion.java` | Neu |
| `InvalidApiVersionException.java` | Neu |
| `ApiVersionExceptionHandler.java` | Neu |
| `TaskV2Dto.java` | Neu |
| `TaskV2ListResponse.java` | Neu |
| `TaskController.java` | Geändert |
| `TaskControllerTest.java` | Geändert |
| `frontend/src/App.jsx` | Geändert |
| `frontend/src/App.test.jsx` | Geändert |
