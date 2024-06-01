package com.example.buspassadmin.Home;
public class Ticket {
    private String busName;
    private String busLicensePlate;
    private String departureTime;
    private String startingLocation;
    private String destination;
    private String ticketPrice;
    private String qrCodeUrl;
    private String ticketID;

    public Ticket() {
        // Default constructor required for Firestore
    }

    public Ticket( String busLicensePlate, String departureTime, String startingLocation, String destination,String ticketPrice, String qrCodeUrl,String ticketID) {
        this.busLicensePlate = busLicensePlate;
        this.departureTime = departureTime;
        this.startingLocation = startingLocation;
        this.destination = destination;
        this.ticketPrice = ticketPrice;
        this.qrCodeUrl = qrCodeUrl;
        this.ticketID =ticketID;
    }


    public  String getTicketID(){return ticketID;}
    public void  setTicketID(String ticketID) {
        this.ticketID = ticketID;
    }
    public String getBusLicensePlate() {
        return busLicensePlate;
    }

    public void setBusLicensePlate(String busLicensePlate) {
        this.busLicensePlate = busLicensePlate;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getStartingLocation() {
        return startingLocation;
    }

    public void setStartingLocation(String startingLocation) {
        this.startingLocation = startingLocation;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getticketPrice() {
        return ticketPrice;
    }

    public void setticketPrice(String ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }
}
