package cpen221.mp3.cache;

public class NotFoundException extends Exception {

    public NotFoundException() { super(); }

    public NotFoundException( String message ) {
            super( message );
        }
}
