**ChatHack**

The ChatHack project is an application that allows users to chat with each other.
To do so, users can chat publicly, by sending messages to all connected users, 
and privately, by chatting with only one other user when he has given his approval.

In private exchange mode, users can exchange messages as well as files.

The following commands allow communication between customers and Broadcast message too :

#Setting up the environment

##MDPServer
first to launch the application and allow users with passwords to log in, you have to
launch the ServerMDP jar with :

#### java -jar ./ServerMDP.jar <port_mdp> <chemin/vers/fichier_de_sauvegarde>


### Message -> send the message in broadcast to all the connected users
### @someoneWhoIsntInGit message -> Send a private message to the client someoneWhoIsntInGit
### /bob file.png -> Send a private file to the client bob
### $accept -> Accept the connection
### $refuse -> Refuse the connection
### & -> Disconnected