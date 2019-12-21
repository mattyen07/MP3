package cpen221.mp3.cache;

import java.time.LocalDateTime;

public class TimePair {

    /*
    RI: this.lastAccess is not null
        this.expiryTime is not null
     */

    /*
    AF(TimePair) is a pair of times such that
        this.lastAccess is the time of last access.
        this.expiryTime is the time of expiry.
     */

    private final LocalDateTime lastAccess;
    private final LocalDateTime expiryTime;

    /**
     * Constructs an immutable pair of times
     * @param lastAccessed time of last access is not null
     * @param expiry time of expiry is not null
     */
    public TimePair(LocalDateTime lastAccessed, LocalDateTime expiry) {
        this.lastAccess = lastAccessed;
        this.expiryTime = expiry;
    }

    /**
     * returns lastAccess time
     * @return this.lastAccess
     */
    public LocalDateTime getLastAccess() {
        return this.lastAccess;
    }

    /**
     * returns expiryTime
     * @return this.expiryTime
     */
    public LocalDateTime getExpiryTime() {
        return this.expiryTime;
    }
}
