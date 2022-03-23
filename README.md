# Distributed Chat Server System
Source code for chat servers that can handle client request and server to server communication are in this repo

### Run Chat Server in local machine
1. Executable server file as `server.jar` and server configuration file as `conf.txt` are provided.
2. Open a command promt in directory which contain `server.jar`.
3. Start server using code<br/>
`java -jar server.jar <server_id> <path_to_conf_file>`. <br/>
As example<br/>
`java -jar server.jar s1 conf.txt`
  1. In here `<server_id>` is server id, which same as in the server id in `conf.txt` file
  2. `<path_to_conf_file>` is path to configuration file


