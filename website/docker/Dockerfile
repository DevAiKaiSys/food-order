# Stage 1: Build Angular application
FROM node:20-alpine AS builder
WORKDIR /app
COPY my-app/package*.json ./
RUN npm ci --legacy-peer-deps
COPY my-app/ .
RUN npm run build -- --configuration production

# Stage 2: Serve with Nginx
FROM nginx:alpine AS production
COPY --from=builder /app/dist/my-app/browser /usr/share/nginx/html
COPY docker/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]