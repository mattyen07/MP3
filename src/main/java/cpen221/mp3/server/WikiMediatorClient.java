package cpen221.mp3.server;

import cpen221.mp3.wikimediator.WikiMediator;

import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.*;
import java.net.UnknownHostException;

import com.google.gson.*;

//https://docs.oracle.com/javase/tutorial/networking/sockets/examples/EchoClient.java
//https://www.baeldung.com/a-guide-to-java-sockets

public class WikiMediatorClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    // Rep invariant: socket, in, out != null

    /**
     * Make a FibonacciClient and connect it to a server running on
     * hostname at the specified port.
     * @throws IOException if can't connect
     */
    public WikiMediatorClient(String hostname, int port) throws IOException {
        socket = new Socket(hostname, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    /**
     * Send a request to the server. Requires this is "open".
     * @param request is a correctly formatted JsonObject
     * @throws IOException if network or server failure
     */
    public void sendRequest(JsonObject request) throws IOException {
        out.print(request);
        out.flush(); // important! make sure x actually gets sent
    }

    /**
     * Get a reply from the next request that was submitted.
     * Requires this is "open".
     * @return the requested Fibonacci number
     * @throws IOException if network or server failure
     */
    public JsonObject getReply() throws IOException {
        JsonParser parser = new JsonParser();
        Gson gson = new Gson();
        JsonObject reply = parser.parse(in).getAsJsonObject();

        return reply;
    }

    /**
     * Closes the client's connection to the server.
     * This client is now "closed". Requires this is "open".
     * @throws IOException if close fails
     */
    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }





    /**
     *
     */
    public static void main(String[] args) {
        try {
            WikiMediatorClient client = new WikiMediatorClient("localhost", WikiMediatorServer.WIKIMEDIATORSERVER_PORT);

            //put junk in here!

            String id = "yeet";
            String type = "simpleSearch";
            String query = "Barack Obama";
            String limit = "12";

            JsonObject request = new JsonObject();
            request.addProperty("id", id);
            request.addProperty("type", type);
            request.addProperty("query", query);
            request.addProperty("limit", limit);

            client.sendRequest(request);
            System.out.println("request sent!:" + request);

            JsonObject reply = client.getReply();

            System.out.println("Reply!:" + reply);

            client.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }






}
