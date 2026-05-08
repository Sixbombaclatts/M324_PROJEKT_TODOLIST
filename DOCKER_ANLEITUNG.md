# 🐳 MySQL mit Docker - Anleitung

## Was ich für dich eingerichtet habe:

### Dateien:
- **`docker-compose.yml`** – Konfiguration für MySQL-Container
- **`.env`** – Umgebungsvariablen (optional)
- **`application.properties`** – Updated mit korrekten Credentials

---

## 🚀 So startest du die Datenbank:

```powershell
# Im Root-Verzeichnis des Projekts
docker-compose up -d
```

**Was passiert:**
- MySQL 8.0 Container wird heruntergeladen und gestartet
- Datenbank `todo_db` wird automatisch erstellt
- User `todouser` mit Passwort `todopass123` wird angelegt
- Daten werden in `mysql_data` Volume persistent gespeichert
- Port `3306` wird auf localhost gebunden

---

## 🧪 Datenbank testen:

```powershell
# Container-Status prüfen
docker-compose ps

# In die Datenbank loggen
docker exec -it todo_mysql mysql -u todouser -p
# Passwort: todopass123
```

Dann im MySQL-Prompt:
```sql
USE todo_db;
SHOW TABLES;
SELECT * FROM tasks;
```

---

## 🛑 Container stoppen/löschen:

```powershell
# Container stoppen (Daten bleiben)
docker-compose down

# Container + Daten löschen
docker-compose down -v
```

---

## 📊 Struktur erklärt:

| Element | Erklärung |
|---------|-----------|
| **version: '3.8'** | Docker-Compose Format-Version |
| **services: mysql** | Definition des MySQL-Services |
| **image: mysql:8.0** | Offizielle MySQL 8.0 Image |
| **environment** | Umgebungsvariablen für MySQL-Setup |
| **ports: 3306:3306** | Container-Port : Host-Port |
| **volumes** | Persistente Speicherung der DB-Daten |
| **healthcheck** | Prüft regelmäßig, ob DB erreichbar ist |
| **networks** | Private Netzwerk für Container |

---

## 🔗 Wie verbindet sich die App:

**application.properties:**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/todo_db
spring.datasource.username=todouser
spring.datasource.password=todopass123
```

Spring Boot Connected automatisch zum Docker-Container auf `localhost:3306` ✅

---

## 💡 Tipps:

1. **Datenbank resetten:**
   ```powershell
   docker-compose down -v
   docker-compose up -d
   ```

2. **Logs anschauen:**
   ```powershell
   docker-compose logs -f mysql
   ```

3. **SQL-Scripts automatisch laden:**
   Datei in `./init.sql` erstellen → wird beim Start ausgeführt

---

## ✅ Jetzt ist alles ready!

1. Terminal öffnen
2. `docker-compose up -d` ausführen
3. `mvn spring-boot:run` im backend-Verzeichnis starten
4. App lädt Tasks aus der **Docker-MySQL**! 🎉
