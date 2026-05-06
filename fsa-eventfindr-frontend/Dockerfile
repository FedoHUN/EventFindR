FROM node:22.14 AS build

WORKDIR /app

# Upgrade npm to match packageManager field in package.json
RUN npm install -g npm@10.4.0

# Copy package.json and package-lock.json
COPY package*.json ./

# Install project dependencies
RUN npm ci

# Copy the entire project
COPY . .

# Build the Angular app for production
RUN npm run build

# Production image — use Nginx to serve Angular app
FROM nginx:alpine

# Copy custom Nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Remove default nginx page, then copy Angular build output
RUN rm -rf /usr/share/nginx/html/*
COPY --from=build /app/dist/fsa-eventfindr-frontend/browser /usr/share/nginx/html
# Angular SSR builds produce index.csr.html instead of index.html
RUN if [ ! -f /usr/share/nginx/html/index.html ]; then \
      cp /usr/share/nginx/html/index.csr.html /usr/share/nginx/html/index.html; \
    fi

# Expose port 80
EXPOSE 80

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]

