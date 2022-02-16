package com.uconnect.backend.search.service;

import java.util.Set;
import com.uconnect.backend.exception.CourseNotFoundException;
import com.uconnect.backend.search.dao.SearchDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

    private final SearchDAO dao;

    @Autowired
    public SearchService(SearchDAO dao) {
        this.dao = dao;
    }
    
    public Set<String> getStudentsByCourse(String name) throws CourseNotFoundException {
        return dao.getStudentsByCourse(name);
    }
}
