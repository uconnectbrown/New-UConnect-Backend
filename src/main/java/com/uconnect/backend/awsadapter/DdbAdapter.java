package com.uconnect.backend.awsadapter;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.ConversionSchemas;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.ScanResultPage;
import com.amazonaws.services.dynamodbv2.model.BillingMode;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.uconnect.backend.exception.UserNotFoundException;
import com.uconnect.backend.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DdbAdapter {
    private final String userTableName;

    private final String emailIndexName;

    private final AmazonDynamoDB ddbClient;

    private final Map<String, DynamoDBMapper> mapperCache;

    private DynamoDBMapper mapper;

    @Autowired
    public DdbAdapter(AmazonDynamoDB ddbClient, String userTableName, String emailIndexName) {
        this.ddbClient = ddbClient;
        this.userTableName = userTableName;
        this.emailIndexName = emailIndexName;
        mapperCache = new ConcurrentHashMap<>();
    }

    public boolean createTableIfNotExists(String tableName, Class<?> clazz, long rcu, long wcu) {
        mapper = new DynamoDBMapper(ddbClient);
        CreateTableRequest request = mapper.generateCreateTableRequest(clazz);
        request.setTableName(tableName);
        request.setProvisionedThroughput(new ProvisionedThroughput(rcu, wcu));
        for (GlobalSecondaryIndex gsi : request.getGlobalSecondaryIndexes()) {
            gsi.setProvisionedThroughput(new ProvisionedThroughput(rcu, wcu));
        }

        return TableUtils.createTableIfNotExists(ddbClient, request);
    }

    public boolean createOnDemandTableIfNotExists(String tableName, Class<?> clazz) {
        mapper = new DynamoDBMapper(ddbClient);
        CreateTableRequest request = mapper.generateCreateTableRequest(clazz);
        request.setTableName(tableName);
        request.setBillingMode(BillingMode.PAY_PER_REQUEST.name());
        if (request.getGlobalSecondaryIndexes() != null) {
            for (GlobalSecondaryIndex gsi : request.getGlobalSecondaryIndexes()) {
                gsi.setProjection(new Projection().withProjectionType(ProjectionType.ALL));
            }
        }

        return TableUtils.createTableIfNotExists(ddbClient, request);
    }

    public boolean deleteTableIfExists(String tableName, Class<?> clazz) {
        mapper = new DynamoDBMapper(ddbClient);
        DeleteTableRequest request = mapper.generateDeleteTableRequest(clazz);
        request.setTableName(tableName);

        return TableUtils.deleteTableIfExists(ddbClient, request);
    }

    public <T> void save(String tableName, T item) {
        setMapperTableName(tableName);
        mapper.save(item);
    }

    public <T> void delete(String tableName, T item) {
        setMapperTableName(tableName);
        mapper.delete(item);
    }

    public <T> List<T> scan(String tableName, Class<T> clazz) {
        setMapperTableName(tableName);
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();

        return mapper.scan(clazz, scanExpression);
    }

    public <T> List<T> scan(String tableName, DynamoDBScanExpression scanExpression, Class<T> clazz) {
        setMapperTableName(tableName);

        return mapper.scan(clazz, scanExpression);
    }

    public <T> ScanResultPage<T> scanPage(String tableName, DynamoDBScanExpression scanExpression, Class<T> clazz) {
        setMapperTableName(tableName);

        return mapper.scanPage(clazz, scanExpression);
    }

    public <T> List<T> query(String tableName, T item, Class<T> clazz) {
        setMapperTableName(tableName);
        DynamoDBQueryExpression<T> queryExpression = new DynamoDBQueryExpression<T>().withHashKeyValues(item);

        return mapper.query(clazz, queryExpression);
    }

    public <T> List<T> queryGSI(String tableName, String indexName, T item, Class<T> clazz) {
        setMapperTableName(tableName);
        DynamoDBQueryExpression<T> queryExpression = new DynamoDBQueryExpression<T>()
                .withIndexName(indexName)
                .withHashKeyValues(item)
                .withConsistentRead(false);

        return mapper.query(clazz, queryExpression);
    }

    private void setMapperTableName(String tableName) {
        DynamoDBMapper cachedMapper = mapperCache.get(tableName);
        if (cachedMapper == null) {
            DynamoDBMapperConfig config = DynamoDBMapperConfig
                    .builder()
                    .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
                    .withConversionSchema(ConversionSchemas.V2)
                    .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES)
                    .build();
            cachedMapper = new DynamoDBMapper(ddbClient, config);

            mapperCache.put(tableName, cachedMapper);
        }

        mapper = cachedMapper;
    }

    public User findByUsername(String username) throws UserNotFoundException {
        User desiredUser = new User();
        desiredUser.setUsername(username);
        List<User> res = queryGSI(userTableName, emailIndexName, desiredUser, User.class);
        if (res.isEmpty()) {
            throw new UserNotFoundException("User not found with username " + username);
        }
        return res.get(0);
    }

    public User findById(String id) throws UserNotFoundException {
        setMapperTableName(userTableName);
        User user = mapper.load(User.class, id);
        if (user == null) {
            throw new UserNotFoundException("User not found with ID " + id);
        }
        return user;
    }

    public boolean existsById(String id) {
        setMapperTableName(userTableName);
        return (mapper.load(User.class, id) != null);
    }
}
