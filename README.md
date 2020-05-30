# ChatHack

The ChatHack project is an application that allows users to chat with each other.
To do so, users can chat publicly, by sending messages to all connected users, 
and privately, by chatting with only one other user when he has given his approval.

In private exchange mode, users can exchange messages as well as files.

In the ChatHack folder, the serverChatHack and clientChatHack jars represent the
final jars of the project, which are already build. In source you have the file 
you want to send and receive and in documentation there is the user documentation, 
the developpement documentation and the javadoc

You need to go on the **ChatHack** directory on your terminal and follow these instructions.

## Compiling and creating JAR files

The ChatHack projet can be compiled using Ant. Type the following commands in order to compile and get the JAR executables in the build directory :

> ``ant init``

> ``ant compile``

> ``ant makeJars``

You can combine the commands above like this :

> ``ant init compile makeJars``

If you want to get a clean repository, you may clean the generated files with the following command :

> ``ant clean``

## Generate JavaDoc

You can also generate the javadoc from the files using ant :

> ``ant javadoc``

To delete the javadoc files, use :

> ``ant clearDoc``

## Setting up the environment

### MDPServer
To launch the server and allow users with passwords to log in, you have to
launch the ServerMDP jar with :

> ``java -jar ./ServerMDP.jar <port_mdp> <path/to/save_file.txt>``

### Server & Client

The ChatHack server is a Jar executable. To launch it, you have to go to the folder where the 
executable is located and do the following command :

> ``java -jar ./ChatHackServer.jar <port_chathack> <address_mdp> <port_mdp>``

Where <port_mdp> is a free port of the machine.

The ChatHack client is a Jar too, To launch it, you have to go to the folder where the 
executable is located and do the following command :

> ``# Without Password``
> ``java -jar ./ChatHackClient.jar "login" <address_chathack> <port_chathack>``

> ``With Password``
> ``java -jar ./ChatHackClient.jar "login" "password" <address_chathack> <port_chathack>``

## Commands available when using the ChatHack Client

* **Message ->** Send the message in broadcast to all the connected users
* **@someoneWhoIsntInGit message ->** Send a private message to the client someoneWhoIsntInGit
* **/bob file ->** Send a private file to the client bob
* **$accept ->** Accept the connection
* **$refuse ->** Refuse the connection
* **& ->** Disconnected


MARTI Emilie - MECHOUK Lisa
