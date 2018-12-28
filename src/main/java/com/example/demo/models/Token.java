package com.example.demo.models;

import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity(name = "token")
@Table(name = "token")
@DynamicUpdate
public class Token {
    @Id
    private int id;
    @NotNull
    @Column(name = "user_nr")
    private String userNr;
    @NotNull
    @Column(name = "access_token")
    private String accessToken;
    @Column(name = "refresh_token")
    private String refreshToken;
    @Column(name = "expires_at")
    private Long expiresAt;
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private User user;

    public Token(){
    }

    public Token(String userId, String accessToken, String refreshToken, Long expiresAt){
        this.userNr = userId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
    }

    public String getUserNr() {
        return userNr;
    }

    public void setUserNr(String userId){
        this.userNr = userId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getId() {
        return id;
    }
}
