package com.uconnect.backend.search.model;

import java.util.Set;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@DynamoDBTable(tableName = "concentration-placeholder")
public class Concentration {
    @EqualsAndHashCode.Include
    @DynamoDBHashKey
    private String name;
    
    @DynamoDBAttribute
    private Set<String> students;
}
