# Kurzanleitung für die Installation der Entwicklungsumgebung zum Basisprojekt im Modul 324

## TLDR

ToDo-Liste mit React (frontend) und Spring (backend). Weitere Details sind in den
Kommentaren vor allem in App.js zu finden.

**Liebe Lernende, bitte FORKT dieses Repo für M324, und macht die Pull-Requests in euren FORKS.**

## Relevante Dateien in den Teil-Projekten (Verzeichnisse):

1. diese Beschreibung
2. frontend (Tools: npm und VSCode)
	* App.js

3. backend (Eclipse oder VS-Code)
	* DemoApplication.java
	* Task.java
	* pom.xml (JAR configuration, mit div. Plugins s.u.)

## Inbetriebnahme

### Voraussetzungen
- Java 21 oder höher
- Maven 3.9+
- Docker und Docker Compose (für MySQL Datenbank)
- Node.js 16+ (für Frontend)

### Backend mit Datenbank starten

#### 1. MySQL-Container starten
```bash
# Im Root-Verzeichnis des Projekts
docker-compose up -d
```

**Was wird gestartet:**
- MySQL 8.0 Container (`todo_mysql`)
- Datenbank: `todo_db`
- User: `todouser` / Password: `todopass123`
- Port: `3306`
- Daten werden persistent in `mysql_data` Volume gespeichert

**Container-Status prüfen:**
```bash
docker-compose ps
```

#### 2. Backend starten
```bash
cd backend
mvn spring-boot:run
```

Das Backend läuft auf `http://localhost:8080` und verbindet sich automatisch zur MySQL-Datenbank.

**Tests laufen:**
```bash
mvn clean test
```

### Frontend starten

```bash
cd frontend
npm install  # Erste Installation
npm run dev
```

Das Frontend läuft auf `http://localhost:5173`

---

## Benutzung

1. http://localhost:5173 zeigt das Frontend an. Hier kann man Tasks eingeben, die sofort darunter in der Liste mit einem *Done*-Button angezeigt werden.
2. Klickt man auf den *Done*-Button eines Tasks wird dieser aus der Liste entfernt (und auch aus der Datenbank gelöscht).
3. Die Task Beschreibungen müssen eindeutig (bzw. einmalig) sein.
4. **Tasks sind nun persistent** – nach einem Neustart des Servers sind die Tasks immer noch da! ✅

---

## Datenbank Management

### Datenbank anschauen
```bash
# In die MySQL-Datenbank einsteigen
docker exec -it todo_mysql mysql -u todouser -ptodopass123 -D todo_db

# Im MySQL-Prompt:
SHOW TABLES;
SELECT * FROM tasks;
```

### Container stoppen (Daten bleiben)
```bash
docker-compose down
```

### Container + Daten löschen (Clean Slate)
```bash
docker-compose down -v
```

---

## Technologie Stack

| Component | Version | Details |
|-----------|---------|---------|
| Java | 21 LTS | Backend Runtime |
| Spring Boot | 3.4.5 | Web & Data Framework |
| MySQL | 8.0 | Persistente Datenbank |
| Hibernate/JPA | 6.6.13 | ORM & Entity Mapping |
| React | Latest | Frontend UI |
| Docker | Latest | Container Runtime |

---

## Architektur

```
Frontend (React)
    ↓ HTTP REST
Backend (Spring Boot)
    ↓ JDBC/JPA
MySQL Database (Docker)
```

### Endpoints

- `GET /` – Alle Tasks abrufen
- `POST /tasks` – Task hinzufügen
- `POST /delete` – Task löschen


### Anstehende Aufgaben

- Erweiterung der Funktionalität durch die Lernenden
- Alternatives Backend für eine VM (WAR Konfiguration)
- Test Umbegung mit Unit-Tests erweitern

(Ausgaben für white-box debugging sind bereits auf den beiden Server vorhanden)
