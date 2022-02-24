package com.uconnect.backend.search.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.uconnect.backend.awsadapter.DdbAdapter;
import com.uconnect.backend.exception.CourseNotFoundException;
import com.uconnect.backend.search.model.CourseRoster;
import com.uconnect.backend.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SearchDAO {

    private final DdbAdapter ddbAdapter;

    private final String userTableName;

    private final String courseTableName;

    private final String classYearIndexName;

    @Autowired
    public SearchDAO(DdbAdapter ddbAdapter, String userTableName,
            String courseTableName, String classYearIndexName) {
        this.ddbAdapter = ddbAdapter;
        this.userTableName = userTableName;
        this.courseTableName = courseTableName;
        this.classYearIndexName = classYearIndexName;
    }

    private CourseRoster findCourseRosterByName(String name)
            throws CourseNotFoundException {
        CourseRoster desiredCourse = new CourseRoster();
        desiredCourse.setName(name);
        List<CourseRoster> res =
                ddbAdapter.query(courseTableName, desiredCourse,
                        CourseRoster.class);
        if (res.isEmpty()) {
            throw new CourseNotFoundException(
                    "Course with name " + name + " not found.");
        }
        return res.get(0);
    }

    public Set<String> getStudentsByCourse(String name)
            throws CourseNotFoundException {
        CourseRoster course = findCourseRosterByName(name);
        return course.getStudents();
    }

    public Set<String> getStudentsByClassYear(String year) {
        User queryUser = new User();
        queryUser.setClassYear(year);
        List<User> students = ddbAdapter.queryGSI(userTableName,
                classYearIndexName, queryUser, User.class);
        Set<String> ret = new HashSet<>();
        for (User u : students) {
            ret.add(u.getUsername());
        }
        return ret;
    }
}
