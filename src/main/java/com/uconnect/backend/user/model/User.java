package com.uconnect.backend.user.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGeneratedKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.uconnect.backend.user.converters.LocationConverter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoDBTable(tableName = "placeholder")
public class User implements UserDetails {
    @DynamoDBHashKey
    @DynamoDBAutoGeneratedKey
    private String id;

    // username = email
    @DynamoDBAttribute
    @DynamoDBIndexHashKey
    private String username;

    @DynamoDBAttribute
    private String password;

    @DynamoDBAttribute
    @DynamoDBIndexHashKey
    private String firstName;

    @DynamoDBAttribute
    @DynamoDBIndexHashKey
    private String lastName;

    @DynamoDBAttribute
    private String classYear;

    // {concentration1, concentration2}
    @DynamoDBAttribute
    private List<String> majors;

    @DynamoDBAttribute
    private String pronouns;

    @DynamoDBTypeConverted(converter = LocationConverter.class)
    @DynamoDBAttribute
    private Location location;

    // {interest1, interest2, interest3}
    @DynamoDBAttribute
    private List<String> interests;

    // list of usernames (emails)
    @DynamoDBAttribute
    private Set<String> sent;

    // list of usernames (emails)
    @DynamoDBAttribute
    private Set<String> pending;

    // list of usernames (emails)
    @DynamoDBAttribute
    private Set<String> connections;

    @DynamoDBAttribute
    private int requests;
    
    @DynamoDBAttribute
    private List<? extends GrantedAuthority> authorities;

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}

