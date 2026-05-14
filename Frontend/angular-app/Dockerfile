# CI build: artifacts are pre-built by Jenkins (npm ci + ng build)
# and copied directly into the nginx image to avoid network calls inside Docker.
FROM nginx:alpine
COPY dist/angular-app/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
