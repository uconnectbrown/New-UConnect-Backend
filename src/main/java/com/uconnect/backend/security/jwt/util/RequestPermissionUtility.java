package com.uconnect.backend.security.jwt.util;

import com.uconnect.backend.exception.UnauthorizedUserRequestException;
import com.uconnect.backend.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RequestPermissionUtility {
    public void authorizeUser(String requestedUsername) {
        User authenticatedUser = (User) (SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        if (StringUtils.isEmpty(authenticatedUser.getUsername()) ||
                !authenticatedUser.getUsername().equals(requestedUsername)) {
            log.info("User \"{}\" just requested something he shouldn't have. Keep an eye on this guy!",
                    authenticatedUser.getUsername());

            throw new UnauthorizedUserRequestException();
        }
    }
}
