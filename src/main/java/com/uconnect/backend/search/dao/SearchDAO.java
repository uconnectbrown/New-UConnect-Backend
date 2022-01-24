package com.uconnect.backend.search.dao;

import java.util.List;
import java.util.Set;
import com.uconnect.backend.awsadapter.DdbAdapter;
import com.uconnect.backend.exception.CourseNotFoundException;
import com.uconnect.backend.search.model.CourseRoster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SearchDAO {

    private final DdbAdapter ddbAdapter;

    private final String courseTableName;

    @Autowired
    public SearchDAO(DdbAdapter ddbAdapter, String userTableName, String courseTableName) {
        this.ddbAdapter = ddbAdapter;
        this.courseTableName = courseTableName;
    }

    private CourseRoster findCourseRosterByName(String name) throws CourseNotFoundException {
        CourseRoster desiredCourse = new CourseRoster();
        desiredCourse.setName(name);
        List<CourseRoster> res =
                ddbAdapter.query(courseTableName, desiredCourse, CourseRoster.class);
        if (res.isEmpty()) {
            throw new CourseNotFoundException("Course with name " + name + " not found.");
        }
        return res.get(0);
    }

    public Set<String> getStudents(String name) throws CourseNotFoundException {
        CourseRoster course = findCourseRosterByName(name);
        return course.getStudents();
    }
}
