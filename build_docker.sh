#!/bin/bash

# build frontend
cd frontend
npm install
npm run build
cd ..
cp -r frontend/dist/ backend/src/main/resources/static/

# build backend
cd backend
./gradlew build -x test

# build docker image
cd ..
docker build -t eventplanner:local -f Dockerfile-local .
