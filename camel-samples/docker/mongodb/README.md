# MongoDB local (Podman)

## Criar rede
```bash
podman network create mongo-net
```

## Subir MongoDB
```bash
podman run -d \
  --name mongodb \
  --network mongo-net \
  -p 27017:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=admin \
  -e MONGO_INITDB_ROOT_PASSWORD=admin \
  mongo:7
```

## Subir Mongo Express
```bash
podman run -d \
  --name mongo-express \
  --network mongo-net \
  -p 8081:8081 \
  -e ME_CONFIG_MONGODB_ADMINUSERNAME=admin \
  -e ME_CONFIG_MONGODB_ADMINPASSWORD=admin \
  -e ME_CONFIG_MONGODB_SERVER=mongodb \
  -e ME_CONFIG_BASICAUTH_USERNAME=admin \
  -e ME_CONFIG_BASICAUTH_PASSWORD=admin \
  mongo-express
```

## Acesso
- Mongo Express: http://localhost:8081 (admin/admin)
- MongoDB: mongodb://admin:admin@localhost:27017