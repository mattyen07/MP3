package cpen221.mp3.cache;

import fastily.jwiki.core.Wiki;


public class CacheObject implements Cacheable {
    private String id;


    public CacheObject (String pageText) {
        this.id = pageText;
    }

    public String id() {
        return this.id;
    }

}
