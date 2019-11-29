package cpen221.mp3.cache;

public class CacheObject implements Cacheable {
    private String id;
    private String text;

    /*
    RI: id and text are not null.
     */

    /*
    AF(o) = a cacheable object such
    o.id() = id of object
    o.text() = text of object
     */

    /**
     * Creates an immutable cacheable object
     * @param id of object
     * @param text stored in object
     */
    public CacheObject(String id, String text) {
        this.id = id;
        this.text = text;
    }

    /**
     * gets id of CacheObject
     * @return id of CacheObject
     */
    public String id() {
        return this.id;
    }

    /**
     * gets text stored in cacheable object
     * @return text
     */
    public String getText() {
        return this.text;
    }
}
