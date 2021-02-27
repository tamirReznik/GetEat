package com.college.geteat.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class Order implements Parcelable {



    private String courierName;
    private String ClientName;


    private boolean hasRated;
    private String clientID;
    private String courierID;
    private String orderNumber;
    private String dateCreated;
    private MyLatLng establishment;
    private MyLatLng target;

    public Order(String clientID, String orderNumber, MyLatLng establishment, MyLatLng target) {
        this.clientID = clientID;
        this.orderNumber = orderNumber;
        this.establishment = establishment;
        this.target = target;
    }



    public Order() {
    }

    public Order(String clientID, String courierID, String orderNumber, MyLatLng establishment, MyLatLng target, String dateCreated) {
        this.clientID = clientID;
        this.courierID = courierID;
        this.orderNumber = orderNumber;
        this.establishment = establishment;
        this.target = target;
        this.dateCreated = dateCreated;
    }

    protected Order(Parcel in) {
        clientID = in.readString();
        courierID = in.readString();
        orderNumber = in.readString();
        establishment = in.readParcelable(LatLng.class.getClassLoader());
        target = in.readParcelable(LatLng.class.getClassLoader());
    }


    public static final Creator<Order> CREATOR = new Creator<Order>() {
        @Override
        public Order createFromParcel(Parcel in) {
            return new Order(in);
        }

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getCourierID() {
        return courierID;
    }

    public void setCourierID(String courierID) {
        this.courierID = courierID;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public MyLatLng getEstablishment() {
        return establishment;
    }

    public void setEstablishment(MyLatLng establishment) {
        this.establishment = establishment;
    }

    public MyLatLng getTarget() {
        return target;
    }

    public void setTarget(MyLatLng target) {
        this.target = target;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getCourierName() {
        return courierName;
    }

    public void setCourierName(String courierName) {
        this.courierName = courierName;
    }

    public String getClientName() {
        return ClientName;
    }

    public void setClientName(String clientName) {
        ClientName = clientName;
    }
    @Override
    public String toString() {
        return "Order{" +
                "courierName='" + courierName + '\'' +
                ", ClientName='" + ClientName + '\'' +
                ", hasRated=" + hasRated +
                ", clientID='" + clientID + '\'' +
                ", courierID='" + courierID + '\'' +
                ", orderNumber='" + orderNumber + '\'' +
                ", dateCreated='" + dateCreated + '\'' +
                ", establishment=" + establishment +
                ", target=" + target +
                '}';
    }

    public boolean isHasRated() {
        return hasRated;
    }

    public void setHasRated(boolean hasRated) {
        this.hasRated = hasRated;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(courierName);
        dest.writeString(ClientName);
        dest.writeByte((byte) (hasRated ? 1 : 0));
        dest.writeString(clientID);
        dest.writeString(courierID);
        dest.writeString(orderNumber);
        dest.writeString(dateCreated);
        dest.writeParcelable(establishment, flags);
        dest.writeParcelable(target, flags);
    }
}
