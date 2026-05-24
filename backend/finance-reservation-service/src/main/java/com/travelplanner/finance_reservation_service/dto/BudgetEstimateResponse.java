package com.travelplanner.finance_reservation_service.dto;

public class BudgetEstimateResponse {

    private Long planId;
    private String destination;
    private int numberOfDays;

    private double accommodationCost;
    private double foodCost;
    private double activitiesCost;
    private double transportCost;

    private double totalEstimatedCost;
    private String currency;

    public BudgetEstimateResponse() {
    }

    public BudgetEstimateResponse(
            Long planId,
            String destination,
            int numberOfDays,
            double accommodationCost,
            double foodCost,
            double activitiesCost,
            double transportCost,
            double totalEstimatedCost,
            String currency
    ) {
        this.planId = planId;
        this.destination = destination;
        this.numberOfDays = numberOfDays;
        this.accommodationCost = accommodationCost;
        this.foodCost = foodCost;
        this.activitiesCost = activitiesCost;
        this.transportCost = transportCost;
        this.totalEstimatedCost = totalEstimatedCost;
        this.currency = currency;
    }

    public Long getPlanId() {
        return planId;
    }

    public String getDestination() {
        return destination;
    }

    public int getNumberOfDays() {
        return numberOfDays;
    }

    public double getAccommodationCost() {
        return accommodationCost;
    }

    public double getFoodCost() {
        return foodCost;
    }

    public double getActivitiesCost() {
        return activitiesCost;
    }

    public double getTransportCost() {
        return transportCost;
    }

    public double getTotalEstimatedCost() {
        return totalEstimatedCost;
    }

    public String getCurrency() {
        return currency;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setNumberOfDays(int numberOfDays) {
        this.numberOfDays = numberOfDays;
    }

    public void setAccommodationCost(double accommodationCost) {
        this.accommodationCost = accommodationCost;
    }

    public void setFoodCost(double foodCost) {
        this.foodCost = foodCost;
    }

    public void setActivitiesCost(double activitiesCost) {
        this.activitiesCost = activitiesCost;
    }

    public void setTransportCost(double transportCost) {
        this.transportCost = transportCost;
    }

    public void setTotalEstimatedCost(double totalEstimatedCost) {
        this.totalEstimatedCost = totalEstimatedCost;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}