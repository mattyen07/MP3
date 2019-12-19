package cpen221.mp3.server;

import cpen221.mp3.wikimediator.WikiMediator;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.*;
import com.google.gson.*;

public class WikiMediatorServer {
    /*
    RI: wmInstance is not null.
        serverSocket is not null.
        maxRequests is not null  and is >= 0
     */

    /*
    AF: WikiMediatorServer is a server that can run the WikiMediator concurrently with multiple clients.
        wmInstance is the instance of the WikiMediator used by the server.
        serverSocket is the main server socket.
        maxRequest is the number of clients the server can handle.
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

    public WikiMediatorServer(int port, int n) throws IOException {
        this.wmInstance = new WikiMediator();
        this.serverSocket = new ServerSocket(port);
        this.maxRequests = n;
    }

    /**
     * Run the server, listening for connections and handling them.
     * @throws IOException if the main server socket is broken
     */
    public void serve() throws IOException {

        while (true) {
            // block until a client connects
            final Socket socket = serverSocket.accept();
            // create a new thread to handle that client
            Thread handler = new Thread(new Runnable() {
                public void run() {
                    try {
                        try {
                            handle(socket);
                        } finally {
                            socket.close();
                        }
                    } catch (IOException ioe) {
                        // this exception wouldn't terminate serve(),
                        // since we're now on a different thread, but
                        // we still need to handle it
                        ioe.printStackTrace();
                    }
                }
            });
            // start the thread
            handler.start();
        }

    }

    /**
     * Handle one client connection. Returns when client disconnects.
     * Parses the JSON request of client such that we can request the appropriate
     * method from the WikiMediator instance
     * @param socket  socket where client is connected
     * @throws IOException if connection encounters an error
     */
    //https://stackoverflow.com/questions/26284419/java-read-json-input-stream
    private void handle(Socket socket) throws IOException {
        InputStreamReader inputStream = new InputStreamReader(socket.getInputStream());
        BufferedReader br = new BufferedReader(inputStream);
        JsonParser parser = new JsonParser();
        Gson gson = new Gson();

        JsonElement json = parser.parse(br);

        if (json.isJsonArray()) {
            JsonArray requestArray = json.getAsJsonArray();
            for (int i = 0; i < requestArray.size(); i++) {
                JsonObject request = requestArray.get(i).getAsJsonObject();
                String id = request.get("id").getAsString();
                String type = request.get("type").getAsString();

            }

        }
    }
    //private final String[] methodNames =
    //            new String[]{"simpleSearch", "getPage", "getConnectedPages",
    //                    "zeitgeist", "trending", "peakLoad30s", "getPath", "executeQuery"};
}
