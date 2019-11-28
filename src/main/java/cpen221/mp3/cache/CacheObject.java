package cpen221.mp3.cache;

public class CacheObject implements Cacheable {
    private String id;
    private String text;

    public CacheObject(String id, String text) {
        this.id = id;
        this.text = text;
    }

    public String id() {
        return this.id;
    }
    public String getText() {
        return this.text;
    }
}
