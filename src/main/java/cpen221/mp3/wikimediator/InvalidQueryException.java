package cpen221.mp3.wikimediator;

import org.antlr.v4.runtime.misc.ParseCancellationException;

public class InvalidQueryException extends ParseCancellationException{
    public InvalidQueryException() {
        super();
    }
}
