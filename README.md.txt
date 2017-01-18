A simple multi-threaded client-server application for transmitting messages and files.  
The server listens for incoming connections from clients and creates a new thread to listen for client requests parallely and routes it to the main thread after some processing. 
The main thread is responsible for responding to these requests. Incoming files can also be uploaded to the server parallely, although any requests from the client will 
have to wait until the file has finished uploading. 

To run this application,

1. Create a server instance by compiling and running the Multiserver.class object

2. Create 1 or more client instances by compiling and running the Client.class object(s).

Note: Change the ipAddress variable if running on different machines.
 
The list of commands are as follows:-
 
1. Unicast - To send a message to a client, use command:    unicast  <ClientID>  <message>

2. Broadcast- To send a message to all clients, use command:   broadcast  <message>

3. Blockcast - To send a message to all clients but one, use command:    blockcast  <ClientID>  <message>

4. File unicast - To send a file to a client use the command unicast-F. The application will then ask for a recipient and filepath respectively.     
                               For e.g.       
                                           unicast-F
                                           Enter recipient:
                                                  0
                                           Enter the file path:
                                                  C:\Users\ABC\Desktop\cn.txt

5. File broadcast - To send a file to all clients use the command broadcast-F. The application will then ask for a filepath.  
                                    For e.g.
                                                  broadcast-F
		          Enter the file path:
		          C:\Users\ABC\Desktop\cn.txt

6. To close the connection - use command:  quit 

Any and all files will be stored in the corresponding folder inside \CN_chatclient\Client-side. For convenience, the folder name will be printed when a client instance is first created. 
The server stores any incoming files in \CN_client\ServerDB. All clients are notified when a client joins or leaves.


 