FROM node:22.14 AS build

WORKDIR /app

# Zjednoti verziu npm s hodnotou packageManager v package.json.
RUN npm install -g npm@10.4.0

# Dependency manifesty kopirujeme samostatne, aby Docker vedel cacheovat npm ci.
COPY package*.json ./
RUN npm ci

COPY . .

# Nginx kontajner potrebuje iba browser build; SSR server build by zbytocne zatazoval CI runner.
ENV NODE_OPTIONS=--max-old-space-size=768
RUN npm run build:static

FROM nginx:alpine

COPY nginx.conf /etc/nginx/nginx.conf

# Nginx image nahradi defaultnu stranku build vystupom aplikacie.
RUN rm -rf /usr/share/nginx/html/*
COPY --from=build /app/dist/fsa-eventfindr-frontend/browser /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
