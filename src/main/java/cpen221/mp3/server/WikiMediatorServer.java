package cpen221.mp3.server;

import cpen221.mp3.wikimediator.WikiMediator;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.*;

public class WikiMediatorServer {
    /*
    RI:
     */

    /*
    AF:
     */

    public static final int WIKIMEDIATORSERVER_PORT = 42069;

    private WikiMediator wmInstance;
    private ServerSocket serverSocket;
    private int maxRequests;

    /**
     * Start a server at a given port number, with the ability to process
     * upto n requests concurrently.
     *
     * @param port the port number to bind the server to.
     *           0 <= port <= 65535
     * @param n > 0 the number of concurrent requests the server can handle
     */

    public WikiMediatorServer(int port, int n) {
        this.wmInstance = new WikiMediator();
        this.maxRequests = n;
    }

    /**
     * Run the server, listening for connections and handling them.
     * @throws IOException if the main server socket is broken
     */
    public void serve() throws IOException {

    }

    /**
     * Handle one client connection. Returns when client disconnects.
     * Parses the JSON request of client such that we can request the appropriate
     * method from the WikiMediator instance
     * @param socket  socket where client is connected
     * @throws IOException if connection encounters an error
     */
    private void handle(Socket socket) throws IOException {

    }


}
