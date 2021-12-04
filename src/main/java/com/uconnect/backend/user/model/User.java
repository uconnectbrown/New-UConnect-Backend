package com.uconnect.backend.user.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGeneratedKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uconnect.backend.user.converters.LocationConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.validation.constraints.Email;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
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
    @Null(message = "New users are assigned random IDs")
    private String id;

    // username = email
    @DynamoDBAttribute
    @DynamoDBIndexHashKey
    @Email(message = "Email is not valid")
    private String username;

    @DynamoDBAttribute
    @Size(min = 8, max = 32, message = "Raw password length must be between 8 and 32 characters (inclusive)")
    private String password;

    @DynamoDBAttribute
    @DynamoDBIndexHashKey
    @Size(min = 1, max = 32, message = "First name length must be between 1 and 32 characters (inclusive)")
    private String firstName;

    @DynamoDBAttribute
    @DynamoDBIndexHashKey
    @Size(min = 1, max = 32, message = "Last name length must be between 1 and 32 characters (inclusive)")
    private String lastName;

    @DynamoDBAttribute
    @Size(min = 4, max = 4, message = "Class year must be between 1000 and 9999 (inclusive)")
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
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return false;
    }
}

