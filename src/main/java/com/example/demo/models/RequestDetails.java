package com.example.demo.models;

import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity(name = "RequestDetails")
@Table(name = "request_details")
@DynamicUpdate
public class RequestDetails {

    @Id
    @GeneratedValue
    private Long id;
    @Column(name = "method")
    @NotNull
    private String method;
    @Column(name = "url")
    @NotNull
    private String url;
    @Column(name = "ip")
    @NotNull
    private String ip;
    @Column(name = "client")
    @NotNull
    private String user;
    @Column(name = "time")
    @NotNull
    private Long time;
    @Column(name = "status")
    @NotNull
    private int status;

    public RequestDetails(){}

    public RequestDetails(String method, String url, String ip, String user, Long time, int status){
        this.method = method;
        this.url = url;
        this.ip = ip;
        this.user = user;
        this.time = time;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
