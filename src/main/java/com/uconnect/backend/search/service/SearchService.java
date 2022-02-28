package com.uconnect.backend.search.service;

import java.util.Set;
import com.uconnect.backend.exception.ConcentrationNotFoundException;
import com.uconnect.backend.exception.CourseNotFoundException;
import com.uconnect.backend.search.dao.SearchDAO;
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
            if (year.length() != 4) {
                throw new IllegalArgumentException("Year must be four digits.");
            } 
            Integer.valueOf(year);
            Set<String> students = dao.getStudentsByClassYear(year);
            return students;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Year must be a number.");
        }
    }

    public Set<String> getStudentsByConcentration(String name) throws ConcentrationNotFoundException {
        return dao.getStudentsByConcentration(name);
    }
}
