# Lissi Eventplanner

The Lissi eventplanner is a webapp for planning sailing legs.

## Local development

Start the backend
```
cd backend
./gradlew bootRun --args='--spring.profiles.active=local,test'
```

Start the frontend
```
cd frontend
npm install
npm run dev
```