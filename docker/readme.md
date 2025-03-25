## Introduction

Dockerfile contains cardano-node, Postgres, Cardano Rosetta API Java implementation

Cardano-node is compiled inside the image from source.  
The api and yaci-indexer modules are compiled inside the image from source.  
Postgres is installed from the repository.

Entrypoint file ``entrypoint.sh`` run all and creates the database and the postgres user.

### 1. Build
```
docker build -t {image_name} -f ./docker/Dockerfile .
```
The build can take up to 1.5 hours.

### 2. Run
````
docker run --env-file ./docker/.env.dockerfile --env-file ./docker/.env.docker-profile-1-entry-level -p 8082:8082 -d {image_name}:latest
````
You need to specify the path to the environment variables file and open the port.

### 3. Changing project settings

All application settings are located in the ``./docker/.env.dockerfile`` file.  
You can specify network, ports, postgres user, etc.

### 3.1 Adjusting hardware profile
Alternatively you can pass another hardware profile to the docker command for more complex setups with higher requirements
in terms of scalability / concurrent users count, a whole list of hardware profiles 
and their description can be found here: https://github.com/cardano-foundation/cardano-rosetta-java/wiki/9.-Hardware-Profiles
``
.env.docker-profile-1-entry-level
.env.docker-profile-2-basic-hardware
.env.docker-profile-3-mid-level
.env.docker-profile-4-advanced-hardware
.env.docker-profile-5-high-performance
.env.docker-profile-6-top-tier-hardware
.env.docker-profile-7-ultimate-performance
``

### 4. Changing build parameters
```
docker build -t {image_name} --build-arg PG_VERSION=14 -f ./docker/Dockerfile .
``` 
You can specify Cabal, GHC, Cardano node, and Postgres versions when building an image.

The default values:  
``
CABAL_VERSION=3.8.1.0
``  
``
GHC_VERSION=8.10.7  
``  
``
CARDANO_NODE_VERSION=10.1.4  
``  
``
PG_VERSION=14  
``

### 5. Volume with Cardano node data
````
docker run --env-file .\docker\.env.dockerfile --env-file ./docker/.env.docker-profile-1-entry-level -p 8082:8082 -v {custom_folder}:/node/db -d {image_name}:latest
````
If you want to use already existing cardano data, you can mount the data volume to the ``/node/db`` folder inside the container to prevent loading during initialization.

### 6. Volume with Postgres data
````
docker run --env-file .\docker\.env.dockerfile --env-file ./docker/.env.docker-profile-1-entry-level -p 8082:8082 -v {custom_folder}:/node/postgres -d {image_name}:latest
````

You can mount a volume with Postgres node data to ``/node/postgres`` point to use already existed data.  
If the mounted volume does not contain the database or empty, new database will be created there.

### 7. Volume with custom network configurations
````
docker run --env-file .\docker\.env.dockerfile --env-file ./docker/.env.docker-profile-1-entry-level -p 8082:8082 -v {custom_folder}:/networks -d {image_name}:latest
````
The cardano node configuration jsons are stored in the ``config`` folder and copied into the image on build.  
If you want to use a custom configuration without rebuilding the image, we can mount a volume with configs to ``/networks`` folder inside the container.

### 8. Synchronization mode
````
docker run -e SYNC=true --env-file .\docker\.env.dockerfile --env-file ./docker/.env.docker-profile-1-entry-level -p 8082:8082 -d {image_name}:latest
````
The container can be started in synchronization mode. In this case, the container will verify chunks and synchronize all nodes when it is started and run ``Rosseta Api`` after that  
To start it you need to change the ``SYNC`` variable in ``.env.dockerfile`` or by adding the ``-e SYNC=true`` key when starting the container.  
Progress can be tracked in the container log.

### 9. Logs location

The logs can be viewed inside the container.  
``
Caradano node - /logs/node.log
``  
``
Yaci indexer - /logs/indexer.log
``  
``
Rosseta Api - /logs/api.log
``  
``
Cardano Submit Api - /logs/submit-api.log
``
``
Yaci Store - /logs/yaci-store.log
``

After starting the container all logs are output to stdout