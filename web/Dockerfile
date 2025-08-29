# Use a Node.js Alpine image for the builder stage
FROM node:22-alpine AS builder
ARG GIT_SHA
ENV GIT_SHA=$GIT_SHA
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build
RUN npm prune --production

# Use another Node.js Alpine image for the final stage
FROM node:22-alpine
ARG GIT_SHA
ENV GIT_SHA=$GIT_SHA
WORKDIR /app
COPY --from=builder /app/build build/
COPY --from=builder /app/node_modules node_modules/
COPY package.json .
EXPOSE 3000
ENV NODE_ENV=production
CMD [ "node", "build" ]