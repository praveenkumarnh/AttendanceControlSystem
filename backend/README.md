# AttendanceControlSystem

Simple API built on top of Vert.x

## Features

- Reactive
- EventBus to avoid communication via HTTP

## Requirements

- Mysql 5.X
- JDK 8 
- Maven

## Setup

- Import the DDL script `./src/main/resources/database/migrations/V1_all_schema.sql`
- Configure the DSN on the file `./src/main/conf/my-application-conf.json`
- And the last one, we need to run the next command:

    $ mvn vertx:run

## Endpoints

### Tracks

`GET` /tracks

`POST` /tracks
```curl
    curl -X POST \
      http://localhost:8083/api/tracks \
      -H 'Content-Type: application/json' \           
      -d '{
            "employeeCode": 2170554
          }'
```

`GET` /tracks/:employee_code `employee_code=[integer]`

### Employee

`POST` /employees/
```curl
    curl -X POST \
      http://localhost:8083/api/employees \
      -H 'Content-Type: application/json' \
      -d '{
            "code": 21705538,
            "firstName": "Jufith",
            "lastName": "Bustamante",
            "email": "rad83+6@gmail.com",
            "avatar": "https://api.randomuser.me/portraits/men/10.jpg"
          }'
```

`PUT` /employees/:code `code=[integer]`
```curl
    curl -X PUT \
      http://localhost:8083/api/employees/2170555 \
      -H 'Content-Type: application/json' \
      -d '{
            "code": 2170553,
            "firstName": "Angelines",
            "lastName": "Fernández",
            "email": "rad8329+3@gmail.com",
            "avatar": "https://api.randomuser.me/portraits/women/10.jpg"
          }'
```

`GET`  /employees/:code `code=[integer]`

`GET` /employees

`DELETE` /employees/:code `code=[integer]`

## EventBus

`WS` /eventbus/*

### Addresses

`tracked.employee`
