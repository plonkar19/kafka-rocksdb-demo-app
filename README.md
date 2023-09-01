# kafka-rocksdb-demo-app

## Run the application on local machine

* To start the application on local machine, execute the following command into project directory 
```bash
./gradlew bootRun
```

## Run the application in docker container
```bash
./gradlew clean build

docker build -t kafka-rocksdb-demo-app:latest .

docker run --name kafka-rocksdb-demo-app -p 8080:8080 -t kafka-rocksdb-demo-app:latest
#docker run -d --name kafka-rocksdb-demo-app --cap-add SYS_ADMIN -p 8080:8080 -t kafka-rocksdb-demo-app:latest
```

## Docker Commands
```bash
docker images

docker ps

docker exec -it kafka-rocksdb-demo-app bash

docker kill kafka-rocksdb-demo-app

docker stop kafka-rocksdb-demo-app

docker rm kafka-rocksdb-demo-app

#docker system prune -a
```