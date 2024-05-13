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
docker run --env-file .\docker\.env.dockerfile -p 8082:8082 -it {image_name}:latest
````
We need to specify the path to the environment variables file and open the port.

### 3. Changing project settings

All application settings are located in the ``.\docker\.env.dockerfile`` file.  
We can specify network, ports, postgres user, etc.

### 4. Changing build parameters
```
docker build -t {image_name} --build-arg PG_VERSION=14 -f ./docker/Dockerfile .
```
We can specify Cabal, GHC, Cardano node, and Postgres versions when building an image.

The default values:  
``
CABAL_VERSION=3.8.1.0
``  
``
GHC_VERSION=8.10.7  
``  
``
CARDANO_NODE_VERSION=8.9.2  
``  
``
PG_VERSION=14  
``

### 5. Volume with Cardano node data
````
docker run --env-file .\docker\.env.dockerfile -p 8082:8082 -v {custom_folder}:/data/db -it {image_name}:latest
````
It is possible to root the cardano node data volumetrically to the /data/db point.

We can mount a volume with Cardano node data to ``/data/db`` to prevent loading during initialization.

### 6. Volume with custom network configurations
````
docker run --env-file .\docker\.env.dockerfile -p 8082:8082 -v {custom_folder}:/networks -it {image_name}:latest
````
The cardano node configuration json's are stored in the ``config`` folder and copied into the image on build.
If we want to use a custom configuration without rebuilding the image, we can mount a volume with configs to ``/config``

### 7. Logs location

The logs can be viewed inside the container.  
``
Caradano node - /logs/node.log
``  
``
Yaci indexer - /logs/indexer.log
``  
``
Api - /logs/api.log
``  