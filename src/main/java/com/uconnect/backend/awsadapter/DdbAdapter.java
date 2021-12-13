package com.uconnect.backend.awsadapter;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.uconnect.backend.exception.UserNotFoundException;
import com.uconnect.backend.user.model.User;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class DdbAdapter {
    @Autowired
    private String userTableName;

    @Autowired
    private String emailIndexName;

    private final AmazonDynamoDB ddbClient;

    private DynamoDBMapper mapper;

    public DdbAdapter(AmazonDynamoDB ddbClient) {
        this.ddbClient = ddbClient;
    }

    public boolean createTableIfNotExists(String tableName, Class<?> clazz, long rcu, long wcu) {
        mapper = new DynamoDBMapper(ddbClient);
        CreateTableRequest request = mapper.generateCreateTableRequest(clazz);
        request.setTableName(tableName);
        request.setProvisionedThroughput(new ProvisionedThroughput(rcu, wcu));

        return TableUtils.createTableIfNotExists(ddbClient, request);
    }

    public <T> void save(String tableName, T item) {
        setMapperTableName(tableName);
        mapper.save(item);
    }

    public <T> void delete(String tableName, T item) {
        setMapperTableName(tableName);
        mapper.delete(item);
    }

    public <T> List<T> scan(String tableName, Class clazz) {
        setMapperTableName(tableName);
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();

        return mapper.scan(clazz, scanExpression);
    }

    public <T> List<T> query(String tableName, T item, Class clazz) {
        setMapperTableName(tableName);
        DynamoDBQueryExpression<T> queryExpression = new DynamoDBQueryExpression<T>().withHashKeyValues(item);

        return mapper.query(clazz, queryExpression);
    }

    public <T> List<T> queryGSI(String tableName, String indexName, T item, Class clazz) {
        setMapperTableName(tableName);
        DynamoDBQueryExpression<T> queryExpression = new DynamoDBQueryExpression<T>()
                .withIndexName(indexName)
                .withHashKeyValues(item)
                .withConsistentRead(false);

        return mapper.query(clazz, queryExpression);
    }

    private void setMapperTableName(String tableName) {
        DynamoDBMapperConfig config = DynamoDBMapperConfig
                .builder()
                .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
                .build();

        mapper = new DynamoDBMapper(ddbClient, config);
    }

    public User findByUsername(String username) throws UserNotFoundException { 
        User desiredUser = new User();
        desiredUser.setUsername(username);
        List<User> res = queryGSI(userTableName, emailIndexName, desiredUser, User.class);
        if (res.isEmpty()) {
            throw new UserNotFoundException("User not found with username " + username);
        } else {
            return res.get(0);
        }
    }

    public User findById(String id) throws UserNotFoundException {
        setMapperTableName(userTableName);
        User user = mapper.load(User.class, id);
        if (user == null) {
            throw new UserNotFoundException("User not found with ID " + id);
        } else {
            return user;
        }
    }

    public boolean existsById(String id) {
        setMapperTableName(userTableName);
        return (mapper.load(User.class, id) != null);
    }
}
