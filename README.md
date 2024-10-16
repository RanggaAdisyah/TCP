Workflow
Server Starts:

The server listens on port 1212.
The server accepts connections from clients.
Client Connects:

The client establishes a connection to the server using the same IP address and port.
Two-Way Communication:

The client sends a message to the server.
The server receives the message and responds with a message from the user.
This process repeats until one party sends the message "exit."
Closing the Connection:

When the communication is finished, both the server and the client close the connection and release the resources used.

Example Interaction

Server: "Server is ready to accept connections..."

Client: "Hello Server!"

Server: "From Client: Hello Server!" (The server reads and prints the message from the client.)

Server: "Server: Welcome!" (The server sends a response.)

Client: "exit" (The client decides to disconnect.)


Server: "Client has disconnected." (The server closes the connection.)
