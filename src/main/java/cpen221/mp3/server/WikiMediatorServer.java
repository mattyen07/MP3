package cpen221.mp3.server;
import cpen221.mp3.wikimediator.WikiMediator;

public class WikiMediatorServer {
    /*
    RI:
     */

    /*
    AF:
     */

    private WikiMediator instance;

    /**
     * Start a server at a given port number, with the ability to process
     * upto n requests concurrently.
     *
     * @param port the port number to bind the server to
     * @param n the number of concurrent requests the server can handle
     */

    public WikiMediatorServer(int port, int n) {
        this.instance = new WikiMediator();


    }



}
