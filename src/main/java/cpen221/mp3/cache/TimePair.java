package cpen221.mp3.cache;

import java.time.LocalDateTime;

public class TimePair {

    private LocalDateTime lastAccess;
    private LocalDateTime expiryTime;

    public TimePair(LocalDateTime lastAccessed, LocalDateTime expiry) {
        this.lastAccess = lastAccessed;
        this.expiryTime = expiry;
    }

    public LocalDateTime getLastAccess() {
        return this.lastAccess;
    }

    public LocalDateTime getExpiryTime() {
        return this.expiryTime;
    }
}
