services:
  mongo:
    image: mongo:latest
    container_name: mongo-db
    restart: unless-stopped
    ports:
      - "27017:27017"
    volumes:
      - mongo-data:/data/db
    environment:
      MONGO_INITDB_ROOT_USERNAME: root
      MONGO_INITDB_ROOT_PASSWORD: secret
      MONGO_INITDB_DATABASE: ai_dating

  app:
    build: .
    image: ai.chat.memory:1.3-SNAPSHOT
    container_name: ai-chat-app
    restart: unless-stopped
    depends_on:
      - mongo
    ports:
      - "8080:8080"
    volumes:
      - ./profiles:/app/profiles
      - ./logs:/logs
    environment:
      SPRING_DATA_MONGODB_URI: mongodb://root:secret@mongo:27017/ai_dating?authSource=admin
      SPRING_PROFILES_ACTIVE: prod
      LOGGING_FILE_NAME: /logs/app.log
      LOGGING_LEVEL_ROOT: INFO
      LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_AI: INFO

volumes:
  mongo-data:
