FROM node:20-alpine

WORKDIR /usr/src/app

# Копируем только файлы с зависимостями
COPY package*.json ./

# Устанавливаем пакеты (с флагами для стабильной сети)
RUN npm install --strict-ssl=false

# Копируем весь остальной код
COPY . .

EXPOSE 3000

# Запускаем в режиме разработки (с hot-reload)
CMD ["npm", "run", "start:dev"]