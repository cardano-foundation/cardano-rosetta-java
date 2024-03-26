## Required plugins for IDEA

To set up work with environment variables in IDEA you need to install [.envfile](https://plugins.jetbrains.com/plugin/7861-envfile) plugin
Use or make a copy from `.env.h2` file and include it for running the applications of this project. 
See the documentation for this plugin for more information.

## Maven profiles    

Open Maven tab in IDEA and add the following profiles:  
- `h2-api` - for running the Rosetta API
- `h2-yaci-indexer` - for running the Yaci Indexer
Click on `Reload All Maven Projects` to apply the changes.


## Setup Yaci Indexer Devkit

### Windows 11

- Setup WSL2 (default for win11)
- Install Ubuntu 22.04 (`wsl --install -d Ubuntu-22.04`)
- Install Docker Desktop for Windows for WSL2

In WSL terminal: 
- git clone https://github.com/bloxbean/yaci-devkit.git 
- replace in yaci-devkit/start.sh `source ./info.sh` with `. ./info.sh`
- run `sudo ./start.sh` in yaci-devkit folder from wsl
- run `sudo ./yaci-start.sh` to run the cli in the container
- yaci-cli:> create-node -o --start # will start cardano node

Useful commands:
- `tip` # get the tip of the blockchain
- `exit` # stop and clear the node
- `reset` # reset the node from the genesis block
- `stop` # stop the node but sometimes after stop it is hard to start the node so use `reset`


## Start the Project

Start YaciIndexerApplication first and then RosettaApiApplication.

http://localhost:9095/h2-console -- H2 Database Console
