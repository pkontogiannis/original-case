# Amadeus API


### Required Dependencies
- Docker 

To make your life easier you can find some of the endpoints in the Postman collection bellow:
https://go.postman.co/workspace/PEM~5daa5545-66c3-48b3-a1eb-52292ccd730d/collection/2002181-559a5a39-abfc-40ac-8d0b-1b8678e5af54

### Improvements
- More metrics per container
- More tests
- Add Swagger file
- Scale up

### Stack
- Akka HTTP
- Slick ORM
- Redis
- PostgreSQL
- JWT for authentication / authorization
- Dockerized application

Please check the `it` folder in the `src` for the Integration Tests

Please check the `test` folder in the `src` for the Unit Tests

### Metrics 
The metrics can be found in the grafana dashboard. 

http://localhost:3000

### Execute
```
- chmod +x run.sh
- ./run.sh
```