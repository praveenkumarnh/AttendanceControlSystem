# AttendanceControlSystem

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

## FatJar

To take advantage of the Vertx web server, we should copy the `/dist` content to `path/to/backend/src/main/resources/webroot/`
after that, we must build de Vertx project as a fatjar