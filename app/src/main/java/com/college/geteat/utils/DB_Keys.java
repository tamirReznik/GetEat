package com.college.geteat.utils;

public interface DB_Keys {

    public enum STATE {
        AUTH_REGISTERED,
        FULL_REGISTERED,
        UNSIGNED
    }

    String CLIENTS = "Clients";
    String COURIERS = "Couriers";
    String ORDERS = "Orders";
    String RESPONSE = "Response";
    String AS_CLIENT = "As_client";
    String AS_COURIER = "As_Courier";
    String APPROVED = "Approved";
    String TRACKER = "Tracker";
    String DETAILS = "Details";
    String PENDING_ORDERS = "Pending_Orders";
    String ACTIVE_ORDERS = "Active_Orders";
    String UID = "uid";
    String USERS = "Users";
    String ACTIVE_COURIERS = "Active_Couriers";
    String COURIERS_PROFILES = "Couriers_Profiles";
    String COMMA = "/";
    String CLIENT = "Client";
    String SUCCESS = "success";
    String DB_STATE = "DB_STATE";

}
