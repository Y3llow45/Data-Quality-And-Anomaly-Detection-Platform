# Data Quality & Anomaly Detection Platform

A full-stack web application that lets users upload CSV datasets, automatically analyze data quality, detect common anomalies (missing values, outliers, low cardinality, etc.), and visualize the results.

Built with **Spring Boot** (backend) + **Svelte** (frontend) + **PostgreSQL**.

## Features

- User authentication (register/login with JWT)
- Upload CSV files to named datasets (multiple versions supported)
- Asynchronous data processing & anomaly detection
- Column-level statistics (type, null rate, min/max/avg/std, distinct count)
- Detected anomalies:
  - High null rate
  - Statistical outliers (Z-score > 3)
  - Very low distinct value count in categorical fields
- Clean, responsive UI showing stats and highlighted anomalies

## Tech Stack

- **Backend**: Spring Boot 3, Spring Data JPA, Hibernate, JWT auth, PostgreSQL
- **Frontend**: SvelteKit (Vite), fetch-based API client
- **Database**: PostgreSQL
- **Async processing**: Spring @Async
- **File storage**: Local filesystem (easy to swap to S3/minio later)

## Screenshots

<img width="1207" height="923" alt="Screenshot (408)" src="https://github.com/user-attachments/assets/5d09de73-4d75-42aa-9d90-71869e693e41" />

## Quick Start

### Backend

1. Configure application.properties (database, jwt secret, etc.)
2. Run
