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

# Copy custom Nginx configuration (optional — if you have one)
# COPY nginx.conf /etc/nginx/nginx.conf

# Copy the built Angular app to Nginx
# Check your package.json build output — default is dist/<project-name>/browser
COPY --from=build /app/dist/fsa-eventfindr-frontend/browser /usr/share/nginx/html

# Expose port 80
EXPOSE 80

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]

