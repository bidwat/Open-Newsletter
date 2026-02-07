# Core Service

This service owns the newsletter data model and publishes campaign send events.

## Quick start (dev)

```bash
./gradlew :core-service:bootRun
```

## Notes

- Requires Postgres and Kafka env vars defined in `core-service/src/main/resources/application.properties`.
- Campaigns start as `DRAFT` and move to `READY` once subject + content are present.
