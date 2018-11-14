# AttendanceControlSystem

## Features

- EventBus to avoid communication via HTTP

## Setup

```bash
yarn install
```

## Build

```bash
yarn run build
```

## Dev

```bash
yarn run start
```

## Deploy

To take advantage of the Vertx web server, the build task copy  
the `/dist` content to `path/to/backend/src/main/resources/webroot/`,
after that we must build the Vertx project as a FatJar
