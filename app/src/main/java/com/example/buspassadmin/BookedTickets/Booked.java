package com.example.buspassadmin.BookedTickets;
public class Booked {

    private String companyName;
    private String ticketPrice;
    private String destination;
    private String startingLocation;
    private String ticketID;
    private String busId;
    private String busLicensePlate;
    private String selectedSeat;
    private String selectedDate;
    private String ticketStatus;

    public Booked() {
        // Default constructor required for Firestore
    }

    public Booked(String destination, String ticketPrice, String ticketID, String companyName, String startingLocation,String busId,String busLicensePlate,String  selectedSeat,String selectedDate,String ticketStatus) {
        this.destination = destination;
        this.companyName = companyName;
        this.ticketID = ticketID;
        this.ticketPrice = ticketPrice;
        this.startingLocation = startingLocation;
        this.busId = busId;
        this.busLicensePlate = busLicensePlate;
        this.selectedSeat = selectedSeat;
        this.selectedDate = selectedDate;
        this.ticketStatus = ticketStatus;
    }

    public String getTicketID() {
        return ticketID;
    }

    public void setTicketID(String ticketID) {
        this.ticketID = ticketID;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(String ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getStartingLocation() {
        return startingLocation;
    }

    public void setStartingLocation(String startingLocation) {
        this.startingLocation = startingLocation;
    }

    public String getBusId() {
        return busId;
    }

    public void setBusId(String busId) {
        this.busId = busId;
    }

    public String getBusLicensePlate() {
        return busLicensePlate;
    }

    public void setBusLicensePlate(String busLicensePlate) {
        this.busLicensePlate = busLicensePlate;
    }

    public String getSelectedSeat() {
        return selectedSeat;
    }

    public void setSelectedSeat(String selectedSeat) {
        this.selectedSeat = selectedSeat;
    }

    public String getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(String selectedDate) {
        this.selectedDate = selectedDate;
    }

    public void setTicketStatus(String ticketStatus) {
        this.ticketStatus = ticketStatus;
    }

    public String getTicketStatus() {
        return ticketStatus;
    }
}