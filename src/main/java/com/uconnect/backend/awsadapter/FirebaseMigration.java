package com.uconnect.backend.awsadapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.uconnect.backend.user.model.Course;
import com.uconnect.backend.user.model.User;
import com.uconnect.backend.user.model.UserCreationType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class FirebaseMigration {
    private static HttpClient client = HttpClients.createDefault();
    private static ObjectMapper mapper = new ObjectMapper();

    @SneakyThrows
    public static void migrate(DdbAdapter ddbAdapter, String userTableName) {
        HttpResponse response = client.execute(new HttpGet(
                "https://us-east4-uconnect-5eebd.cloudfunctions.net/api/getEmails"));

        List<String> emails = mapper.readValue(EntityUtils.toString(response.getEntity()), List.class);
        for (String email : emails) {
            log.info("Handling user {}", email.split("@")[0]);

            String userUrl = String.format("https://us-east4-uconnect-5eebd.cloudfunctions.net/api/user/%s",
                    email.split("@")[0]);
            response = client.execute(new HttpGet(String.format("https://us-east4-uconnect-5eebd.cloudfunctions.net/api/user/%s",
                    email.split("@")[0])));

            String userString = EntityUtils.toString(response.getEntity());
            JsonNode node = mapper.readValue(userString, JsonNode.class);
            node = node.get("user");
            boolean isComplete = node.get("firstTime").asText().equals("true");
            String username = node.get("email").asText();
            ((ObjectNode) node).remove("featured");
            ((ObjectNode) node).remove("firstTime");
            ((ObjectNode) node).remove("email");

            User user = mapper.treeToValue(node, User.class);
            user.setCreationType(UserCreationType.O_AUTH);
            user.setIsVerified(true);
            user.setIsProfileCompleted(!isComplete);
            user.setUsername(username);

            List<String> majors = user.getMajors();
            List<String> nonEmptyMajors = new ArrayList<>();
            for (String major : majors) {
                if (!StringUtils.isEmpty(major)) {
                    nonEmptyMajors.add(major);
                }
            }
            user.setMajors(nonEmptyMajors);

            Set<Course> courses = user.getCourses();
            Set<Course> ret = new HashSet<>();
            for (Course s : courses) {
                if (!StringUtils.isEmpty(s.getCode()) && !StringUtils.isEmpty(s.getName())) {
                    ret.add(s);
                }
            }
            user.setCourses(ret);

            user.setGroups(clean(user.getGroups()));
            user.setPickUpSports(clean(user.getPickUpSports()));
            user.setVarsitySports(clean(user.getVarsitySports()));
            user.setInstruments(clean(user.getInstruments()));
            user.setSent(clean(user.getSent()));
            user.setPending(clean(user.getPending()));
            user.setConnections(clean(user.getConnections()));

            user.setReceivedRequests(clean(new HashSet<>()));
            user.setAuthorities(new ArrayList<>());

            ddbAdapter.save(userTableName, user);
        }
    }

    private static Set<String> clean(Set<String> set) {
        Set<String> ret = new HashSet<>();
        ret.add("");
        if (set == null) {
            return ret;
        }

        for (String s : set) {
            if (!StringUtils.isEmpty(s)) {
                ret.add(s);
            }
        }

        return ret;
    }
}
