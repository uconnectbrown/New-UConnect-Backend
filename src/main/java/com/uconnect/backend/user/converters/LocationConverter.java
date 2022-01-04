package com.uconnect.backend.user.converters;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.uconnect.backend.user.model.Location;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class LocationConverter implements DynamoDBTypeConverter<List<String>, Location> {
    @Override
    public List<String> convert(Location object) {
        try {
            List<String> location = Arrays.asList(object.getCountry(), object.getState(), object.getCity());
            return location;
        } catch (Exception e) {
            log.error("An error occurred in converting location to string array", e);
            return new ArrayList<>();
        }
    }


    @Override
    public Location unconvert(List<String> object) {
        try {
            Location location = new Location();
            location.setCountry(object.get(0));
            location.setState(object.get(1));
            location.setCity(object.get(2));

            return location;
        } catch (Exception e) {
            log.error("An error occurred in converting string array to location", e);
            return new Location();
        }
    }
}
