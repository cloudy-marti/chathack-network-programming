# ChatHack

The ChatHack project is an application that allows users to chat with each other.
To do so, users can chat publicly, by sending messages to all connected users, 
and privately, by chatting with only one other user when he has given his approval.

In private exchange mode, users can exchange messages as well as files.

The following commands allow communication between customers and Broadcast message too :

# Setting up the environment

## MDPServer
first to launch the application and allow users with passwords to log in, you have to
launch the ServerMDP jar with :

#### java -jar ./ServerMDP.jar <port_mdp> <chemin/vers/fichier_de_sauvegarde>

# Server & Client

The ChatHack server is a Jar  To launch it, you have to go to the folder where the 
executable is located and do the following command :

#### java -jar ./ChatHackServer <port_chathack> <address_mdp> <port_mdp>

where <port_mdp> is a free port of the machine.

The ChatHack client is a Jar too, To launch it, you have to go to the folder where the 
executable is located and do the following command :

#### (Without Password) java -jar ./ChatHackClient "login" <address_chathack> <port_chathack>

#### (With Password) java -jar ./ChatHackClient "login" "password" <address_chathack> <port_chathack>

# Command to be used once the server and client are running :

### Message -> send the message in broadcast to all the connected users
### @someoneWhoIsntInGit message -> Send a private message to the client someoneWhoIsntInGit
### /bob file.png -> Send a private file to the client bob
### $accept -> Accept the connection
### $refuse -> Refuse the connection
### & -> Disconnected