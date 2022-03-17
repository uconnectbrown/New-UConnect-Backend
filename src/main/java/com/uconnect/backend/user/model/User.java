package com.uconnect.backend.user.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGeneratedKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel.DynamoDBAttributeType;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTyped;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uconnect.backend.security.authority.UserAuthority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
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
    @EqualsAndHashCode.Include
    @DynamoDBAttribute
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "emailIndex")
    @NotBlank(message = "User object username cannot be null or empty")
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
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "classYearIndex")
    @Digits(integer = 4, fraction = 1, message = "Class year must be a number with at most 4 integral digits and 1 fractional digit")
    private String classYear;

    // {concentration1, concentration2}
    @DynamoDBAttribute
    private List<String> majors;

    @DynamoDBAttribute
    private String pronouns;

    @DynamoDBAttribute
    private Date createdAt;

    @DynamoDBAttribute
    private String imageUrl;

    @DynamoDBAttribute
    @Size(max = 250, message = "User bio cannot be longer than 250 characters")
    private String bio;

    @DynamoDBAttribute
    @Size(max = 20, message = "Greek life name cannot be longer than 20 characters")
    private String greekLife;

    @DynamoDBAttribute
    private Location location;

    @DynamoDBAttribute
    private List<InterestItem> interests1;

    @DynamoDBAttribute
    private List<InterestItem> interests2;

    @DynamoDBAttribute
    private List<InterestItem> interests3;

    @DynamoDBAttribute
    private Set<Course> courses;

    @DynamoDBAttribute
    private Set<String> groups;

    @DynamoDBAttribute
    private Set<String> pickUpSports;

    @DynamoDBAttribute
    private Set<String> varsitySports;

    @DynamoDBAttribute
    private Set<String> instruments;

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
    private Set<String> receivedRequests;

    // no validation needed, always manually set to false for new users
    @DynamoDBAttribute
    private Boolean isVerified;

    @DynamoDBAttribute
    private Boolean isProfileCompleted;

    @DynamoDBAttribute
    private List<UserAuthority> authorities;

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

