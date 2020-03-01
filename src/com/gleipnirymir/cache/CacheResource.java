package com.gleipnirymir.cache;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class CacheResource {

    private String method;
    private String key;
    private String savedResource;
    private Instant due;

    public CacheResource(String method, String key, String savedResource, int minutesAlive) {
        this.method = method;
        this.key = key;
        this.savedResource = savedResource;
        this.due = Instant.now().plus(minutesAlive, ChronoUnit.MINUTES);
    }

    public boolean isValid(){
        return Instant.now().isBefore(this.due);
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSavedResource() {
        return savedResource;
    }

    public void setSavedResource(String savedResource) {
        this.savedResource = savedResource;
    }

    public Instant getDue() {
        return due;
    }

    public void setDue(Instant due) {
        this.due = due;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheResource that = (CacheResource) o;
        return Objects.equals(method, that.method) &&
                Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, key);
    }

}
