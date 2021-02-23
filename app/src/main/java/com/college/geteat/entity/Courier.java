package com.college.geteat.entity;

public class Courier {

    private int rank = 0;
    private double radius = 0;
    private boolean isActive;
    private int orderCounter = 0;
    private String name = "";

    public MyLatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(MyLatLng latLng) {
        this.latLng = latLng;
    }

    private MyLatLng latLng;
    private String uid = "";
    private String imageUrl = "";
    private String address = "";

    public Courier(int rank, double radius, boolean isActive, int orderCounter, String name, String uid, String imageUrl, String address, MyLatLng latLng) {
        this.rank = rank;
        this.radius = radius;
        this.isActive = isActive;
        this.orderCounter = orderCounter;
        this.name = name;
        this.uid = uid;
        this.imageUrl = imageUrl;
        this.address = address;
        this.latLng = latLng;
    }

    public Courier() {
    }

    public int getRank() {
        return rank;
    }

    public Courier setRank(int rank) {
        this.rank = rank;
        return this;
    }

    public int getOrderCounter() {
        return orderCounter;
    }

    public Courier setOrderCounter(int orderCounter) {
        this.orderCounter = orderCounter;
        return this;
    }

    public double getRadius() {
        return radius;
    }

    public String getName() {
        return name;
    }

    public Courier setName(String name) {
        this.name = name;
        return this;
    }

    public String getUid() {
        return uid;
    }

    public Courier setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Courier setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public Courier setAddress(String address) {
        this.address = address;
        return this;
    }

    public Courier setRadius(double radius) {
        this.radius = radius;
        return this;
    }

    public boolean isActive() {
        return isActive;
    }

    public Courier setActive(boolean active) {
        isActive = active;
        return this;
    }


}
