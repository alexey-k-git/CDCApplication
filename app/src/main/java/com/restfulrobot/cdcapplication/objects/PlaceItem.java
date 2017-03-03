package com.restfulrobot.cdcapplication.objects;


public class PlaceItem {
    private int id;
    private String comment;
    private String address;
    private Coordinar coordinar;
    private double distance;

    public PlaceItem(int id, String comment, String address, Coordinar coordinar) {
        this.id = id;
        this.comment = comment;
        this.address = address;
        this.coordinar = coordinar;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public Coordinar getCoordinar() {
        return coordinar;
    }

}
