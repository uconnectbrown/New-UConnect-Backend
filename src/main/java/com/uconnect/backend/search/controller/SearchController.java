package com.uconnect.backend.search.controller;

import java.util.Set;
import com.uconnect.backend.exception.CourseNotFoundException;
import com.uconnect.backend.search.service.SearchService;
import com.uconnect.backend.security.RequestPermissionUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

    /**
     * Gets the students in acourse.
     * <p>
     * Responses:
     * <ul>
     * <li> OK - successful </li>
     * <li> NOT_FOUND - course does not exist </li>
     * <li> INTERNAL_SERVER_ERROR - unexpected exception occurred </li> 
     * </ul>
     * 
     * @param name the name of the course
     * @return a set of student usernames
     */ 
    @GetMapping("/v1/search/getStudents")
    public ResponseEntity<Set<String>> getStudents(@RequestHeader(name = "Course") String name, @RequestHeader(name = "Username") String username) {
        requestPermissionUtility.authorizeUser(username);
        try {
            Set<String> students = searchService.getStudents(name);
            return new ResponseEntity<>(students, HttpStatus.OK);
        } catch (CourseNotFoundException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Unexpected exception encountered when trying to get course {}: {}", name, e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
