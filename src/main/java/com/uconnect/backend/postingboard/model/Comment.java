package com.uconnect.backend.postingboard.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGeneratedKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.uconnect.backend.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@DynamoDBTable(tableName = "placeholder")
public class Comment {
    @DynamoDBHashKey
    @DynamoDBAutoGeneratedKey
    @Null(message = "New comments are assigned random IDs")
    @EqualsAndHashCode.Include
    private String id;

    @DynamoDBAttribute
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "parentIdIndex")
    @EqualsAndHashCode.Include
    @NotNull(message = "New comments must have a parent ID")
    private String parentId;

    @DynamoDBAttribute
    private Date timestamp;

    @DynamoDBAttribute
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "authorIndex")
    private String author;

    @DynamoDBAttribute
    @NotBlank(message = "New comments must contain at least one character as their content")
    @Size(max = 1000, min = 1, message = "Comment content length must be between 1 and 1000 characters")
    private String content;

    @DynamoDBAttribute
    private Boolean anonymous;

    @DynamoDBAttribute
    private ReactionCollection reactions;

    @DynamoDBAttribute
    private Boolean commentPresent;

    @DynamoDBIgnore
    private User authorInfo;

    @DynamoDBIgnore
    private List<Comment> comments;
}
