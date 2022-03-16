package com.uconnect.backend.search.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.uconnect.backend.awsadapter.DdbAdapter;
import com.uconnect.backend.exception.ConcentrationNotFoundException;
import com.uconnect.backend.exception.CourseNotFoundException;
import com.uconnect.backend.search.model.Concentration;
import com.uconnect.backend.search.model.CourseRoster;
import com.uconnect.backend.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class SearchDAO {

    private final DdbAdapter ddbAdapter;

    private final String userTableName;

    private final String courseTableName;

    private final String classYearIndexName;

    private final String concentrationTableName;

    private final String firstNameBucketIndexName;

    private final String lastNameBucketIndexName;

    @Autowired
    public SearchDAO(DdbAdapter ddbAdapter, String userTableName,
            String courseTableName, String classYearIndexName,
            String concentrationTableName, String firstNameBucketIndexName,
            String lastNameBucketIndexName) {
        this.ddbAdapter = ddbAdapter;
        this.userTableName = userTableName;
        this.courseTableName = courseTableName;
        this.classYearIndexName = classYearIndexName;
        this.concentrationTableName = concentrationTableName;
        this.firstNameBucketIndexName = firstNameBucketIndexName;
        this.lastNameBucketIndexName = lastNameBucketIndexName;
    }

    private CourseRoster findCourseRosterByName(String name)
            throws CourseNotFoundException {
        CourseRoster desiredCourse = new CourseRoster();
        desiredCourse.setName(name);
        List<CourseRoster> res =
                ddbAdapter.query(courseTableName, desiredCourse,
                        CourseRoster.class);
        if (res.isEmpty()) {
            log.info("Could not find course: {}", name);
            throw new CourseNotFoundException(
                    "Course with name " + name + " not found.");
        }
        return res.get(0);
    }

    private Concentration findConcentrationByName(String name)
            throws ConcentrationNotFoundException {
        Concentration desiredConcentration = new Concentration();
        desiredConcentration.setName(name);
        List<Concentration> res = ddbAdapter.query(concentrationTableName,
                desiredConcentration, Concentration.class);
        if (res.isEmpty()) {
            log.info("Could not find concentration: {}", name);
            throw new ConcentrationNotFoundException("Concentration not found: " + name);
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

    public Set<String> getStudentsByConcentration(String name)
            throws ConcentrationNotFoundException {
        Concentration concentration = findConcentrationByName(name);
        return concentration.getStudents();
    }

    public Set<User> getStudentsInFirstNameBucket(char bucket) {
        User query = new User();
        query.setFirstNameBucket(bucket);
        List<User> queryRes = ddbAdapter.queryGSI(userTableName, firstNameBucketIndexName, query, User.class);
        return new HashSet<>(queryRes);
    }

    public Set<User> getStudentsInLastNameBucket(char bucket) {
        User query = new User();
        query.setLastNameBucket(bucket);
        List<User> queryRes = ddbAdapter.queryGSI(userTableName, lastNameBucketIndexName, query, User.class);
        return new HashSet<>(queryRes);
    }
}
