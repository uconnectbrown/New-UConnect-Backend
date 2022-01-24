package com.uconnect.backend.search.dao;

import com.uconnect.backend.awsadapter.DdbAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SearchDAO {

    private final DdbAdapter ddbAdapter;

    private final String userTableName;

    @Autowired
    public SearchDAO(DdbAdapter ddbAdapter, String userTableName) {
        this.ddbAdapter = ddbAdapter;
        this.userTableName = userTableName;
    }
    
}
