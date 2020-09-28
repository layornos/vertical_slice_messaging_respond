Deploying the repository
------------------------

1. Run `./gradlew process_repository:shadowJar` from the parent directory to build the repository
2. Create a `.env` file with entries `MYSQL_ROOT_PASSWORD` (A randomly generated secret) and `BROKER` (The url of the mqtt broker in the form of `tcp://127.0.0.1`)
3. Run `docker-compose up`
