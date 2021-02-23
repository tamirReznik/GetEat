package com.college.geteat.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;

public class Client implements Parcelable {

    private String name = "";
    private String imageUrl = "";
    private String address = "";
    private MyLatLng latLng;

    public Client() {
    }

    public Client(String name, String imageUrl, String address, MyLatLng latLng) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.address = address;
        this.latLng = latLng;
    }


    protected Client(Parcel in) {
        name = in.readString();
        imageUrl = in.readString();
        address = in.readString();
        latLng = in.readParcelable(MyLatLng.class.getClassLoader());
    }

    public static final Creator<Client> CREATOR = new Creator<Client>() {
        @Override
        public Client createFromParcel(Parcel in) {
            return new Client(in);
        }

        @Override
        public Client[] newArray(int size) {
            return new Client[size];
        }
    };

    public String getName() {
        return name;
    }

    public Client setName(String name) {
        this.name = name;
        return this;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Client setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public Client setAddress(String address) {
        this.address = address;
        return this;
    }

    public MyLatLng getLatLng() {
        return latLng;
    }

    public Client setLatLng(MyLatLng latLng) {
        this.latLng = latLng;
        return this;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(imageUrl);
        dest.writeString(address);
        dest.writeParcelable(latLng, flags);
    }
}