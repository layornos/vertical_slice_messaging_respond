Deploying the repository
------------------------

1. Run `./gradlew process_repository:shadowJar` from the parent directory to build the repository
2. Create a `.env` file with entries `MYSQL_ROOT_PASSWORD` (A randomly generated secret) and `BROKER` (The url of the mqtt broker in the form of `tcp://127.0.0.1`). Setting `CREATE_DEMO_PLANTS` to `1` will create plants with the name `default`, `demo1` and `demo2`.
3. Run `docker-compose up`
