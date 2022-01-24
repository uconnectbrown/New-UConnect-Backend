package com.uconnect.backend.search.controller;

import com.uconnect.backend.search.service.SearchService;
import com.uconnect.backend.security.jwt.util.RequestPermissionUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchController {

    private final SearchService searchService;

    private final RequestPermissionUtility requestPermissionUtility;

    @Autowired
    public SearchController(SearchService searchService,
            RequestPermissionUtility requestPermissionUtility) {
        this.searchService = searchService;
        this.requestPermissionUtility = requestPermissionUtility;
    }

}
