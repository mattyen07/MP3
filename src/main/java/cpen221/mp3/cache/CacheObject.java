package cpen221.mp3.cache;

import fastily.jwiki.core.Wiki;


public class CacheObject implements Cacheable {
    private String id;
    private String text;

    public CacheObject (String id, Wiki wiki) {
        this.id = id;
        this.text = wiki.getPageText(id);
    }

    public String id() {
        return this.id;
    }
    public String getText(){
        return this.text;
    }
}
