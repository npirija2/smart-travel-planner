# Stage 1: Build faza (koristi laganu Node.js alpine sliku)
FROM node:20-alpine AS build
WORKDIR /app

# Kopiranje package.json fajlova kako bi se iskoristio Docker cache za zavisnosti
COPY package*.json ./
RUN npm ci

# Definišemo varijablu koja će tokom build-a povezati frontend sa API Gateway-om
# Pošto unutar Docker mreže klijent (browser) i dalje pristupa preko tvog računara,
# gateway-u pristupamo preko localhost:8080 (kako je definisano u tvom .env fajlu)
ARG VITE_API_URL=http://localhost:8080/api
ENV VITE_API_URL=$VITE_API_URL

# Kopiranje ostatka izvornog koda i pokretanje produkcionog build-a
COPY . .
RUN npm run build

# Stage 2: Produkciona faza (Serviranje statičkih fajlova preko Nginx-a)
FROM nginx:stable-alpine

# Kopiranje generisanih fajlova iz Vite-ovog 'dist' foldera u Nginx direktorijum
COPY --from=build /app/dist /usr/share/nginx/html

# Kopiranje prilagođene nginx konfiguracije napravljene u Koraku 1
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Eksponiramo port 80 unutar kontejnera
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]