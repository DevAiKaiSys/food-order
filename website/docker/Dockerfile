# Stage 1: Build Angular application
FROM node:20-alpine AS builder
WORKDIR /app
COPY my-app/package*.json ./
RUN npm ci --legacy-peer-deps
COPY my-app/ .
RUN npm run build -- --configuration production

# Stage 2: Serve with Nginx
FROM nginx:alpine AS production

# Install gettext for envsubst command
RUN apk add --no-cache gettext

# Copy built Angular app
COPY --from=builder /app/dist/my-app/browser /usr/share/nginx/html

# Copy nginx config
COPY docker/nginx.conf /etc/nginx/conf.d/default.conf

# Copy config template
COPY docker/config.template.js /usr/share/nginx/html/config.template.js

# Copy and set entrypoint
COPY docker/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 80

ENTRYPOINT ["/entrypoint.sh"]