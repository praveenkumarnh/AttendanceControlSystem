# AttendanceControlSystem

Simple API built on top of Vert.x

## Endpoints

### Tracks

`GET` /tracks

`POST` /tracks/
```
    curl --request GET -url 'http://localhost:8083/api/tracks'
```

`GET` /tracks/:employee_code `employee_code=[integer]`

### Employee

`POST` /employees/
```
    curl --request POST \
         --url 'http://localhost:8083/api/employees' \
         --header 'Content-Type: application/json' \
         --data '{
                "code": 21705538,
                "firstName": "Ruben",
                "lastName": "Aguirre",
                "email": "Ruben+6@gmail.com",
                "avatar": null
        }'
```

`PUT` /employees/:code `code=[integer]`
```
    curl --request PUT \
         --url 'http://localhost:8083/api/employees/2170555' \
         --header 'Content-Type: application/json' \
         --data '{
            "code": 2170553,
            "firstName": "Ramón",
            "lastName": "Valdez",
            "email": "moncho+3@gmail.com",
            "avatar": null
        }'
```

`GET`  /employees/:code `code=[integer]`

`GET` /employees

`DELETE` /employees/:code `code=[integer]`
