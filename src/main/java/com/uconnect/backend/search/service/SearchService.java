package com.uconnect.backend.search.service;

import java.util.Set;
import com.uconnect.backend.exception.ConcentrationNotFoundException;
import com.uconnect.backend.exception.CourseNotFoundException;
import com.uconnect.backend.search.dao.SearchDAO;
import com.uconnect.backend.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SearchService {

    private final SearchDAO dao;

    @Autowired
    public SearchService(SearchDAO dao) {
        this.dao = dao;
    }
    
    public Set<String> getStudentsByCourse(String name) throws CourseNotFoundException {
        return dao.getStudentsByCourse(name);
    }

    public Set<String> getStudentsByClassYear(String year) throws IllegalArgumentException {
        // Ensure that the year is valid
        try {
            if (year.equals("null")) {
                throw new IllegalArgumentException("Year cannot be null.");
            }
            double yearDouble = Double.valueOf(year);
            if (yearDouble < 1000 || yearDouble > 9999) {
                throw new IllegalArgumentException("Year must be four digits.");
            } else if (yearDouble % 0.5 != 0) {
                throw new IllegalArgumentException("Year must be divisible by 0.5.");
            }
            Set<String> students = dao.getStudentsByClassYear(year);
            return students;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Year must be a number.");
        }
    }

    public Set<String> getStudentsByConcentration(String name) throws ConcentrationNotFoundException {
        return dao.getStudentsByConcentration(name);
    }

    public Set<User> getStudentsByName(String name) throws IllegalArgumentException {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty string.");
        }

        char bucket = Character.toLowerCase(name.charAt(0));
        Set<User> firstNameMatches = dao.getStudentsInFirstNameBucket(bucket);
        Set<User> lastNameMatches = dao.getStudentsInLastNameBucket(bucket);
        firstNameMatches.addAll(lastNameMatches);
        return firstNameMatches;
    }
}
