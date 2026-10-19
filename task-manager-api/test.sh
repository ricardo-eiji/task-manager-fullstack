#!/bin/bash

# Runs the tests against the Docker database 
# (start it first: docker compose up -d db)

SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/task_manager \
SPRING_DATASOURCE_USERNAME=taskuser \
SPRING_DATASOURCE_PASSWORD="${DB_PASSWORD:?Set DB_PASSWORD first}" \
mvn test

# How to run in the terminal? 
    # > DB_PASSWORD=taskpass ./test.sh

    