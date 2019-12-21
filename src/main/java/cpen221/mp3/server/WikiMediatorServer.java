package cpen221.mp3.server;

import cpen221.mp3.wikimediator.WikiMediator;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

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
    private static final String FAILLURE_STATUS = "failed";
    private static final String SUCCESS_STATUS = "success";

    private WikiMediator wmInstance;
    private ServerSocket serverSocket;
    private int maxRequests;
    private int numCurrentRequests;

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
        this.numCurrentRequests = 0;
    }

    /**
     * Run the server, listening for connections and handling them.
     * @throws IOException if the main server socket is broken
     */
    public void serve() throws IOException {

        while (true) {
            // block until a client connects
            final Socket socket = serverSocket.accept();

            if(this.numCurrentRequests < this.maxRequests) {
                synchronized (this) {
                    this.numCurrentRequests++;
                }
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
            } else {
                //if there are too many requests being handled disconnect client.
                PrintWriter out = new PrintWriter(new OutputStreamWriter(
                        socket.getOutputStream()), true);
                out.println("Sorry Server is full :(");
                out.close();
                socket.close();
            }


        }

    }

    /**
     * Handle one client connection. Returns when client disconnects.
     * Parses the JSON request of client such that we can request the appropriate
     * method from the WikiMediator instance
     * @param socket  socket where client is connected
     * @throws IOException if connection encounters an error
     */
    private void handle(Socket socket) throws IOException {
        System.err.println("client connected");


        //load previous stats from file:
        wmInstance.loadRequestsFromFile();
        wmInstance.loadStatsFromFile();
        wmInstance.loadStartTimeFromFile();


        // get the socket's input stream, and wrap converters around it
        // that convert it from a byte stream to a character stream,
        // and that buffer it so that we can read a line at a time
        BufferedReader in = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));

        // similarly, wrap character=>bytestream converter around the
        // socket output stream, and wrap a PrintWriter around that so
        // that we have more convenient ways to write Java primitive
        // types to it.
        PrintWriter out = new PrintWriter(new OutputStreamWriter(
                socket.getOutputStream()), true);

        try {
            JsonParser parser = new JsonParser();
            JsonObject returningObject = new JsonObject();
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                JsonObject request = parser.parse(line).getAsJsonObject();

                if(request.has("timeout")) {
                    int timeout = Integer.parseInt(request.get("timeout").getAsString().replaceAll(",", ""));
                    ExecutorService executorService = Executors.newSingleThreadExecutor();

                    Future<JsonObject> result = executorService.submit(new Callable<JsonObject>() {
                        @Override
                        public JsonObject call() throws Exception {
                            return getWikiReply(request);
                        }
                    });

                    try {
                        returningObject = result.get(timeout, TimeUnit.SECONDS);
                    } catch (InterruptedException | ExecutionException e) {

                        returningObject.addProperty("id", request.get("id").getAsString());
                        returningObject.addProperty("status", this.FAILLURE_STATUS);
                        returningObject.addProperty("response", "Execution Failed");

                    } catch (TimeoutException e) {

                        returningObject.addProperty("id", request.get("id").getAsString());
                        returningObject.addProperty("status", this.FAILLURE_STATUS);
                        returningObject.addProperty("response", "Operation timed out");

                    }

                    executorService.shutdownNow();


                } else {
                    returningObject = getWikiReply(request);
                }



                    //write stats to file!
                wmInstance.writeRequestsToFile();
                wmInstance.writeStatsToFile();
                wmInstance.writeStartTimeToFile();

                out.println(returningObject.toString() + "\r\n");
            }
        } finally {
            out.close();
            in.close();
            synchronized (this) {
                this.numCurrentRequests--;
            }
        }
    }


    /**
     * Helper method to get the correct JSON formatted reply from WikiMediator based on the request.
     * @param request is a correctly formatted JsonObject for the server.
     * @return correctly formatted reply containing the results of the wikimediator.
     */
    private JsonObject getWikiReply(JsonObject request) {

        Gson gson = new Gson();
        JsonObject returningObject = new JsonObject();

        String id = request.get("id").getAsString();
        String type = request.get("type").getAsString();

        if (type.equals("simpleSearch")) {
            String query = request.get("query").getAsString();
            int limit = request.get("limit").getAsInt();
            List<String> result = this.wmInstance.simpleSearch(query, limit);


            returningObject.addProperty("id", id);
            returningObject.addProperty("status", this.SUCCESS_STATUS);
            returningObject.addProperty("response", gson.toJson(result));

        } else if (type.equals("getPage")) {
            String pageTitle = request.get("pageTitle").getAsString();
            String result = this.wmInstance.getPage(pageTitle);

            returningObject.addProperty("id", id);
            returningObject.addProperty("status", this.SUCCESS_STATUS);
            returningObject.addProperty("response", gson.toJson(result));

        } else if (type.equals("getConnectedPages")) {
            String pageTitle = request.get("pageTitle").getAsString();
            int hops = request.get("hops").getAsInt();
            List<String> result = this.wmInstance.getConnectedPages(pageTitle, hops);

            returningObject.addProperty("id", id);
            returningObject.addProperty("status", this.SUCCESS_STATUS);
            returningObject.addProperty("response", gson.toJson(result));

        } else if (type.equals("zeitgeist")) {
            int limit = request.get("limit").getAsInt();
            List<String> result = this.wmInstance.zeitgeist(limit);

            returningObject.addProperty("id", id);
            returningObject.addProperty("status", this.SUCCESS_STATUS);
            returningObject.addProperty("response", gson.toJson(result));

        } else if (type.equals("trending")) {
            int limit = request.get("limit").getAsInt();
            List<String> result = this.wmInstance.trending(limit);

            returningObject.addProperty("id", id);
            returningObject.addProperty("status", this.SUCCESS_STATUS);
            returningObject.addProperty("response", gson.toJson(result));

        } else if (type.equals("peakLoad30s")) {
            int result = this.wmInstance.peakLoad30s();

            returningObject.addProperty("id", id);
            returningObject.addProperty("status", this.SUCCESS_STATUS);
            returningObject.addProperty("response", gson.toJson(result));

        } else if (type.equals("getPath")) {
            String startPage = request.get("startPage").getAsString();
            String stopPage = request.get("stopPage").getAsString();
            List<String> result = this.wmInstance.getPath(startPage, stopPage);

            returningObject.addProperty("id", id);
            returningObject.addProperty("status", this.SUCCESS_STATUS);
            returningObject.addProperty("response", gson.toJson(result));

        } else if (type.equals("executeQuery")) {
            String query = request.get("query").getAsString();
            List<String> result = this.wmInstance.executeQuery(query);

            returningObject.addProperty("id", id);
            returningObject.addProperty("status", this.SUCCESS_STATUS);
            returningObject.addProperty("response", gson.toJson(result));

        }

        return returningObject;
    }



    /**
     * Start a FibonacciServer running on the default port.
     */
    public static void main(String[] args) {
        try {
            WikiMediatorServer server = new WikiMediatorServer(WIKIMEDIATORSERVER_PORT, 1);
            server.serve();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //private final String[] methodNames =
    //            new String[]{"simpleSearch", "getPage", "getConnectedPages",
    //                    "zeitgeist", "trending", "peakLoad30s", "getPath", "executeQuery"};

    /**
     * Handle one client connection. Returns when client disconnects.
     * Parses the JSON request of client such that we can request the appropriate
     * method from the WikiMediator instance
     * @param socket  socket where client is connected
     * @throws IOException if connection encounters an error
     */
    //https://stackoverflow.com/questions/26284419/java-read-json-input-stream
    /*private void handle(Socket socket) throws IOException {
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
    */



}

