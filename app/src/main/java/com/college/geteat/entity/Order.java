package com.college.geteat.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

public class Order implements Parcelable {
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

    @Override
    public String toString() {
        return "Order{" +
                "clientID='" + clientID + '\'' +
                ", courierID='" + courierID + '\'' +
                ", orderNumber='" + orderNumber + '\'' +
                ", dateCreated='" + dateCreated + '\'' +
                ", establishment=" + establishment +
                ", target=" + target +
                '}';
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(clientID);
        dest.writeString(courierID);
        dest.writeString(orderNumber);
        dest.writeString(dateCreated);
        dest.writeParcelable(establishment, flags);
        dest.writeParcelable(target, flags);
    }


//    public Order(String clientID, String courierID, int orderNumber, Place establishment, Place target, Date dateCreated, Date pickUpDate) {
//        this.clientID = clientID;
//        this.courierID = courierID;
//        this.orderNumber = orderNumber;
//        this.dateCreated = dateCreated;
//        this.pickUpDate = pickUpDate;
//    }
//
//    private Date dateCreated;
//    private Date pickUpDate;
//
//    public Order() {
//    }
//
//    public Order(String clientID, Place establishment, Date pickUpDate) {
//        this.clientID = clientID;
//        this.establishment = establishment;
//        this.pickUpDate = pickUpDate;
//    }
//
//    public Order(String clientID, Place target, Place establishment, Date pickUpDate) {
//        this.clientID = clientID;
//        this.establishment = establishment;
//        this.pickUpDate = pickUpDate;
//        this.target = target;
//    }
//
//    protected Order(Parcel in) {
//        orderNumber = in.readInt();
//        establishment = in.readParcelable(Place.class.getClassLoader());
//    }
//
//    public static final Creator<Order> CREATOR = new Creator<Order>() {
//        @Override
//        public Order createFromParcel(Parcel in) {
//            return new Order(in);
//        }
//
//        @Override
//        public Order[] newArray(int size) {
//            return new Order[size];
//        }
//    };
//
//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeInt(orderNumber);
//        dest.writeParcelable(establishment, flags);
//    }
//
//
//    public Place getTarget() {
//        return target;
//    }
//
//    public void setTarget(Place target) {
//        this.target = target;
//    }
//
//    @Override
//    public String toString() {
//        return "Order{" +
//                "clientID='" + clientID + '\'' +
//                ", courierID='" + courierID + '\'' +
//                ", orderNumber=" + orderNumber +
//                ", establishment=" + establishment +
//                ", target=" + target +
//                ", dateCreated=" + dateCreated +
//                ", pickUpDate=" + pickUpDate +
//                '}';
//    }
//
//    public String getClientID() {
//        return clientID;
//    }
//
//    public void setClientID(String clientID) {
//        this.clientID = clientID;
//    }
//
//    public String getCourierID() {
//        return courierID;
//    }
//
//    public void setCourierID(String courierID) {
//        this.courierID = courierID;
//    }
//
//    public int getOrderNumber() {
//        return orderNumber;
//    }
//
//    public void setOrderNumber(int orderNumber) {
//        this.orderNumber = orderNumber;
//    }
//
//    public Place getEstablishment() {
//        return establishment;
//    }
//
//    public void setEstablishment(Place establishment) {
//        this.establishment = establishment;
//    }
//
//    public Date getDateCreated() {
//        return dateCreated;
//    }
//
//    public void setDateCreated(Date dateCreated) {
//        this.dateCreated = dateCreated;
//    }
//
//    public Date getPickUpDate() {
//        return pickUpDate;
//    }
//
//    public void setPickUpDate(Date pickUpDate) {
//        this.pickUpDate = pickUpDate;
//    }

}
