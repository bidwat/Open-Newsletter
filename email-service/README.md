# Email Service

This service consumes send-campaign events and emits delivery status events.

- Kafka bootstrap servers must be set via `KAFKA_BOOTSTRAP_SERVERS`.
- Current implementation logs sends and publishes `SENT` status events.
