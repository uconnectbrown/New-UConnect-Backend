package com.uconnect.backend.user.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGeneratedKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@DynamoDBTable(tableName = "userInfo")
public class User {
    @DynamoDBHashKey
    @DynamoDBAutoGeneratedKey
    private String id;

    @DynamoDBAttribute
    private String username;

    @DynamoDBAttribute
    private String password;

    @DynamoDBAttribute
    private String emailAddress;

    @DynamoDBAttribute
    private Set<String> learningLanguages;

    @DynamoDBAttribute
    private Set<String> nativeLanguages;
}

