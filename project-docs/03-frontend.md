# Frontend Documentation

## Overview
The frontend is a single **Angular 18** SPA (Single Page Application) located under `Frontend/angular-app/`.

## Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Angular | 18.2.x | Framework |
| TypeScript | ~5.5.2 | Language |
| RxJS | ~7.8.0 | Reactive programming |
| Tailwind CSS | 3.4.19 | Utility-first styling |
| PostCSS | ^8.5.6 | CSS processing |
| Autoprefixer | ^10.4.24 | CSS vendor prefixes |

## Scripts (package.json)

```bash
npm run start      # ng serve (dev server)
npm run build      # ng build (production)
npm run watch      # ng build --watch --configuration development
npm run test       # ng test (Karma + Jasmine)
```

## Docker Support

### Dockerfile (`Frontend/angular-app/Dockerfile`)
```dockerfile
# Stage 1: Build
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build --prod

# Stage 2: Serve
FROM nginx:alpine
COPY --from=build /app/dist/angular-app /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

**Key points:**
- Multi-stage build: Node.js builds, Nginx serves.
- Uses `nginx.conf` (custom config) for SPA routing (fallback to `index.html`).
- Exposes port `80` inside the container; mapped to host `4200` in `docker-compose.yml`.

## Integration with Backend

- The Angular app talks to the **API Gateway** at `http://localhost:8090` (or `http://api-gateway:8090` from inside Docker).
- CORS is pre-configured on the gateway for `http://localhost:4200` and `http://frontend:80`.
- JWT tokens are stored (presumably in `localStorage` or a service) and sent in the `Authorization: Bearer <token>` header.

## Development Workflow

```bash
cd Frontend/angular-app
npm install
ng serve
# Navigate to http://localhost:4200
```

## Notes
- The root `package.json` provides convenience npm scripts:
  - `npm run install:frontend`
  - `npm run start:frontend`
- Tailwind CSS is configured (presence in `devDependencies`), but verify `tailwind.config.js` exists if custom theming is needed.
