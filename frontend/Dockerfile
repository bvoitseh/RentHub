# Frontend image: build the SPA with Node, serve it with Nginx, which also proxies /api and /photos
# (infra/nginx/renthub.conf.template). Base images: node (MIT), nginx (BSD-2-Clause).
FROM node:24-alpine AS build
WORKDIR /src
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:1.28-alpine
COPY --from=build /src/dist /usr/share/nginx/html
EXPOSE 80
