package com.imprezer.controllers;

import com.imprezer.model.FullEvent;
import com.imprezer.model.EventCategory;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Created by robert on 14.06.16.
 */
public class EventDataValidator {

    private EventDataValidator() {
        //util class => private constructor
    }

    /**
     * Validates event.
     *
     * @param event
     * @return error message or empty string if all fields are valid
     */
    public static String validateEvent(FullEvent event) {
        //validate name
        StringBuilder errorMessage = new StringBuilder();

        //name
        errorMessage.append(validateName(event.getName()));

        errorMessage.append(validateCategories(event.getCategories()));

//        errorMessage.append(validateDateTimeNotPast("Begin", event.getBeginDateTime()));
//        errorMessage.append(validateDateTimeNotPast("End", event.getEndDateTime()));

//        errorMessage.append(validateDatesRelationShip(event.getBeginDateTime(), event.getEndDateTime()));

        errorMessage.append(validateDetailsLength(event.getDetails()));
        errorMessage.append(validateCoordinatesScope(event.getLocation()));

        errorMessage.append(validatePlace(event.getPlace()));

        return errorMessage.toString();
    }

    private static String validatePlace(String place) {
        return place == null || place.trim().isEmpty() ? "Place not provided." : "";
    }

    private static String validateCoordinatesScope(double[] location) {
        return location.length != 2 ? "Location must consist of 2 values: latitude and longitude." :
                location[0] < -90 || location[0] > 90 ? "Latitude out of scope. " :
                        location[1] < -180 || location[1] > 180 ? "Longitude out of scope. " : "";
    }

    private static String validateDetailsLength(String details) {
        return details == null || details.trim().isEmpty() ? "Details not provided" :
                details.trim().length() > 1000 ? "Description too long. " : "";
    }

    private static String validateDatesRelationShip(ZonedDateTime beginDateTime, ZonedDateTime endDateTime) {
        return beginDateTime == null || endDateTime == null ? "Missing date: cannot verify dates relationship. " :
                !beginDateTime.isBefore(endDateTime) ? "End date-time before begin date-time. " :
                        beginDateTime.plusDays(3).isBefore(endDateTime) ? "Event takes more than 3 days. " : "";

    }

    private static String validateDateTimeNotPast(String dateRef, ZonedDateTime dateTime) {
        return dateTime == null ? dateRef + " date-time not provided. " :
                ZonedDateTime.now().isBefore(dateTime) ? "" : dateRef + " date-time is not a future date. ";
    }

    private static String validateCategories(List<EventCategory> categories) {
        return categories == null ? "" :
                categories.size() < 1 ?
                        "Minimum one category is required. " :
                        categories.size() > 3 ?
                                "Maximum three categories can be assigned to event" :
                                "";
    }

    private static String validateName(String name) {
        return name == null ? "No name provided. " :
                name.length() < 500 ? "" : "Name longer than 500 signs. ";
    }
}
