# Frontend

This is the frontend for the eventDetails planner app.

## Prerequisites

To run the frontend locally you need `node` version 22 and npm installed.
In addition, the frontend needs the backend to run on port 8081 for local development.

## Getting started

### Install dependencies

```bash
npm install
```

### Starting the dev server

This starts the frontend on http://localhost:8080.

```bash
npm run dev
```

### Building

These steps are also executed in the pipeline and will break the build, when an error is found.  
It is recommended to run these scripts at least once before commiting.

```bash
npm run lint
npm run format
npm run build
```

## Dependencies

- [Node 22](https://nodejs.org/en/)
- [Vite](https://vitejs.dev/)
- [Vue 3](https://v3.vuejs.org/)
- [Tailwind CSS](https://tailwindcss.com/)
- [ESLint](https://eslint.org/)
- [Prettier](https://prettier.io/)
- [Vue I18n](https://vue-i18n.intlify.dev/)
