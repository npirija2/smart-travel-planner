package com.travelplanner.planning_service.dto;

import java.time.LocalDate;

import com.travelplanner.planning_service.model.Destination;

public class TravelPlanBasicResponse {

    private Long id;
    private String title;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;

    public TravelPlanBasicResponse() {
    }

    public TravelPlanBasicResponse(Long id, String title, String destination, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.title = title;
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public TravelPlanBasicResponse(Long id2, Object title2, Destination destination2, LocalDate startDate2,
            LocalDate endDate2) {
        //TODO Auto-generated constructor stub
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDestination() {
        return destination;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}