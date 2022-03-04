package com.uconnect.backend.postingboard.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoDBDocument
public class ReactionCollection {
    private Set<String> likeUsernames;
    private int likeCount;

    private Set<String> loveUsernames;
    private int loveCount;

    private Set<String> interestedUsernames;
    private int interestedCount;
}
