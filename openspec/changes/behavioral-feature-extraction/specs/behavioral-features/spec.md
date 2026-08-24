## Purpose

Определяет contract-first набор explainable behavioral features для одного
subject: семантику, формулы и JSON Schema response для on-demand вычисления в
`behavior-analysis-service` и будущего потребления `ai-detection-service`.

## ADDED Requirements

### Requirement: Feature set is fixed and explainable
Behavioral features SHALL form a fixed, documented set with explainable
semantics suitable for anomaly explanation.

#### Scenario: Feature set is inspected
- **WHEN** features API is reviewed
- **THEN** feature set SHALL include `new_ip_device`, `unusual_time`,
  `request_rate` and `download_volume`
- **AND** each feature SHALL have a documented semantic and unit

### Requirement: New IP/device feature is derived
`new_ip_device` SHALL describe subject activity from IP addresses or devices not
previously observed for that subject.

#### Scenario: Feature is computed over a window
- **WHEN** subject features are requested
- **THEN** feature SHALL count IPs and devices in the window that were not
  observed in the subject's history before the window
- **AND** result SHALL be explainable in terms of specific new IPs or devices

### Requirement: Unusual time feature is derived
`unusual_time` SHALL describe how much subject activity falls outside the
subject's typical activity hours.

#### Scenario: Feature is computed over a window
- **WHEN** subject features are requested
- **THEN** feature SHALL compare events in the window against the subject's
  historical activity pattern
- **AND** result SHALL be explainable in terms of time-of-day deviation

### Requirement: Request rate feature is derived
`request_rate` SHALL describe the rate of subject events over a configured
window.

#### Scenario: Feature is computed over a window
- **WHEN** subject features are requested
- **THEN** feature SHALL count events for the subject within the window
- **AND** result SHALL be expressed as events per time unit

### Requirement: Download volume feature is derived
`download_volume` SHALL describe the volume of download-related subject
activity over a configured window.

#### Scenario: Feature is computed over a window
- **WHEN** subject features are requested
- **THEN** feature SHALL sum volume of download-type events for the subject
  within the window
- **AND** volume SHALL be read from numeric `metadata.bytes` of download-type
  events, with missing or non-numeric values contributing zero
- **AND** result SHALL be expressed in bytes

### Requirement: Feature response has a stable schema
Subject features response SHALL have a stable JSON Schema shared between
`behavior-analysis-service` and future consumers.

#### Scenario: Response schema is inspected
- **WHEN** features endpoint is called
- **THEN** response SHALL include subject identifier, computed feature values,
  their windows, computation timestamp and short explanations
