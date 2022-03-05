package com.uconnect.backend.search.controller;

import java.util.Set;
import com.uconnect.backend.exception.ConcentrationNotFoundException;
import com.uconnect.backend.exception.CourseNotFoundException;
import com.uconnect.backend.search.service.SearchService;
import com.uconnect.backend.security.RequestPermissionUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
     * Gets the students in a course.
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
    @GetMapping("/v1/search/getStudentsByCourse/{name}")
    public ResponseEntity<Set<String>> getStudentsByCourse(@PathVariable("name") String name) {
        log.info("Received a query for students in course: %s", name);
        try {
            Set<String> students = searchService.getStudentsByCourse(name);
            return new ResponseEntity<>(students, HttpStatus.OK);
        } catch (CourseNotFoundException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Unexpected exception encountered when trying to get course {}: {}", name, e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/v1/search/getStudentsByClassYear/{year}")
    public ResponseEntity<?> getStudentsByClassYear(@PathVariable("year") String year) {
        try {
            Set<String> students = searchService.getStudentsByClassYear(year);
            return new ResponseEntity<>(students, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Unexpected exception encountered when trying to get students in class year: {}", year);
            return new ResponseEntity<>("Unexpected exception occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/v1/search/getStudentsByConcentration/{concentration}")
    public ResponseEntity<?> getStudentsByConcentration(@PathVariable("concentration") String concentration) {
        try {
            Set<String> students = searchService.getStudentsByConcentration(concentration);
            return new ResponseEntity<>(students, HttpStatus.OK);
        } catch (ConcentrationNotFoundException e) {
            return new ResponseEntity<>("Could not find concentration: " + concentration, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Unexpected exception encountered when trying to get students in concentration {}: {}", concentration, e);
            return new ResponseEntity<>("Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
