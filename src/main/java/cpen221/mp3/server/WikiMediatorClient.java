package cpen221.mp3.server;

import cpen221.mp3.wikimediator.WikiMediator;

import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.*;
import java.net.UnknownHostException;

import com.google.gson.*;

//https://docs.oracle.com/javase/tutorial/networking/sockets/examples/EchoClient.java

public class WikiMediatorClient {
    private static String hostName = "localHost";

    public WikiMediatorClient(int portNumber) {
        try(
                Socket socket = new Socket(InetAddress.getLocalHost(), 42069);
                ) {

        } catch(UnknownHostException e) {
            System.out.println("Unknown Host Exception");
        } catch(java.io.IOException e) {
            System.out.println("Couldn't get I/O for the connection to " + hostName);
        }
    }




}
