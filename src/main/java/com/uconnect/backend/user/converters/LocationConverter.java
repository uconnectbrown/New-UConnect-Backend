package com.uconnect.backend.user.converters;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.uconnect.backend.user.model.Location;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocationConverter implements DynamoDBTypeConverter<String[], Location> {
    @Override
    public String[] convert(Location object) {
        try {
            String[] location = {object.getCountry(), object.getState(), object.getCity()};
            return location;
        } catch (Exception e) {
            log.error("An error occurred in converting location to string array", e);
            return new String[3];
        }
    }


    @Override
    public Location unconvert(String[] object) {
        try {
            Location location = new Location();
            location.setCountry(object[0]);
            location.setState(object[1]);
            location.setCity(object[2]);

            return location;
        } catch (Exception e) {
            log.error("An error occurred in converting string array to location", e);
            return new Location();
        }
    }
}
