FROM node:24-alpine
WORKDIR /app
COPY build build/
COPY node_modules node_modules/
COPY package.json .
EXPOSE 3000
ENV NODE_ENV=production
CMD [ "node", "build" ]