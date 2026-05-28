FROM node:22.14 AS build

WORKDIR /app

# Zjednoti verziu npm s hodnotou packageManager v package.json.
RUN npm install -g npm@10.4.0

# Dependency manifesty kopirujeme samostatne, aby Docker vedel cacheovat npm ci.
COPY package*.json ./
RUN npm ci

COPY . .

# Produkcny Angular build generuje staticke subory do dist/.
RUN npm run build

FROM nginx:alpine

COPY nginx.conf /etc/nginx/nginx.conf

# Nginx image nahradi defaultnu stranku build vystupom aplikacie.
RUN rm -rf /usr/share/nginx/html/*
COPY --from=build /app/dist/fsa-eventfindr-frontend/browser /usr/share/nginx/html

# Angular SSR build moze vytvorit index.csr.html; Nginx ocakava index.html.
RUN if [ ! -f /usr/share/nginx/html/index.html ]; then \
      cp /usr/share/nginx/html/index.csr.html /usr/share/nginx/html/index.html; \
    fi

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
