package com.uconnect.backend.user.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGeneratedKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel.DynamoDBAttributeType;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTyped;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uconnect.backend.user.converters.LocationConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode
@Builder
@DynamoDBTable(tableName = "placeholder")
public class User implements UserDetails {
    @DynamoDBHashKey
    @DynamoDBAutoGeneratedKey
    @Null(message = "New users are assigned random IDs")
    private String id;

    @DynamoDBAttribute
    @DynamoDBTyped(DynamoDBAttributeType.S)
    private UserCreationType creationType;

    // username = email
    @DynamoDBAttribute
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "emailIndex")
    @Email(message = "Email is not valid")
    private String username;

    @DynamoDBAttribute
    @Size(min = 8, max = 32, message = "Raw password length must be between 8 and 32 characters (inclusive)")
    private String password;

    @DynamoDBAttribute
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "firstName")
    @Size(min = 1, max = 32, message = "First name length must be between 1 and 32 characters (inclusive)")
    private String firstName;

    @DynamoDBAttribute
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "lastName")
    @Size(min = 1, max = 32, message = "Last name length must be between 1 and 32 characters (inclusive)")
    private String lastName;

    @DynamoDBAttribute
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "classYear")
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

    // no validation needed, always manually set to false for new users
    @DynamoDBAttribute
    private boolean isVerified;

    @EqualsAndHashCode.Exclude
    @DynamoDBAttribute
    private List<? extends GrantedAuthority> authorities;

    @Override
    @JsonIgnore
    @DynamoDBIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    @DynamoDBIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    @DynamoDBIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    @DynamoDBIgnore
    public boolean isEnabled() {
        return true;
    }
}

