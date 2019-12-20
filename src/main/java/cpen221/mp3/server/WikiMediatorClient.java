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
    public FibonacciClient(String hostname, int port) throws IOException {
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
        String reply = in.readLine();
        if (reply == null) {
            throw new IOException("connection terminated unexpectedly");
        }

        try {
            return new JsonObject(reply);
        } catch (NumberFormatException nfe) {
            throw new IOException("misformatted reply: " + reply);
        }
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




    private static final int N = 100;

    /**
     * Use a FibonacciServer to find the first N Fibonacci numbers.
     */
    public static void main(String[] args) {
        try {
            FibonacciClient client = new FibonacciClient("localhost", FibonacciServer.FIBONACCI_PORT);

            // send the requests to find the first N Fibonacci numbers
            for (int x = 1; x <= N; ++x) {
                client.sendRequest(x);
                System.out.println("fibonacci("+x+") = ?");
            }

            // collect the replies
            for (int x = 1; x <= N; ++x) {
                BigInteger y = client.getReply();
                System.out.println("fibonacci("+x+") = "+y);
            }

            client.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }






}
