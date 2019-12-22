package cpen221.mp3.server;

import cpen221.mp3.wikimediator.WikiMediator;

import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.*;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

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
    public void sendRequest(String request) throws IOException {
        out.print(request + "\r\n");
        out.flush();
    }

    /**
     * Get a reply from the next request that was submitted.
     * Requires this is "open".
     * @return the requested Fibonacci number
     * @throws IOException if network or server failure
     */
    public String getReply() throws IOException {
        JsonParser parser = new JsonParser();
        String reply = "error";
        Gson gson = new Gson();
        reply = in.readLine();
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

            String id = "test1";
            JsonObject oneR = new JsonObject();
            oneR.addProperty("id", id);
            oneR.addProperty("type", "test");


            String two = "test2";
            JsonObject twoR = new JsonObject();
            twoR.addProperty("id", two);
            twoR.addProperty("type", "test");


            client.sendRequest(oneR.toString());
            System.err.println("OneR: "+ oneR.toString());

            String reply = client.getReply();

            client.sendRequest(twoR.toString());
            System.err.println("twoR: "+ twoR.toString());

            String reply2 = client.getReply();

            System.err.println("First:" + reply + "end");
            System.err.println("Second:" + reply2 + "end");


            client.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }






}
