Deploying the repository
------------------------

1. Create a `.env` file with entries `MYSQL_ROOT_PASSWORD` (A randomly generated secret) and `BROKER` (The url of the mqtt broker in the form of `tcp://127.0.0.1`) in `docker/`. Setting `CREATE_DEMO_PLANTS` to `1` will create plants with the name `default`, `demo1` and `demo2`.

Then you have multiple options:
1. Deploy docker containers on your machine:
    Run `./build.sh` from this directory to build the repository
    Run `docker-compose up` in the `docker/` directory.


2. Deploy a virtual machine with all the services installed:
    Run `vagrant up`

3. Deploy to your own server:
    Use `ansible-playbook deploy.yml`. The server needs to be in group `respond_repository`
