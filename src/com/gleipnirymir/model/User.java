package com.gleipnirymir.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "user")
public class User implements Serializable {

    @Id
    @Column(name = "discordAccountId")
    private long discordAccountId;

    @Column(name = "crAccountTag")
    private String crAccountTag;

    public User() {
    }

    public User(long discordAccountId, String crAccountTag) {
        this.discordAccountId = discordAccountId;
        this.crAccountTag = crAccountTag;
    }

    public long getDiscordAccountId() {
        return discordAccountId;
    }

    public void setDiscordAccountId(long discordAccountId) {
        this.discordAccountId = discordAccountId;
    }

    public String getCrAccountTag() {
        return crAccountTag;
    }

    public void setCrAccountTag(String crAccountTag) {
        this.crAccountTag = crAccountTag;
    }

    @Override
    public String toString() {
        return "User: " + this.discordAccountId + ", " + this.crAccountTag;
    }
}
