package org.revo.Domain;

import lombok.Getter;

import static org.revo.Domain.Role.Paths.*;

/**
 * Created by ashraf on 17/04/17.
 */
@Getter
public enum Role {
    USER("USER", USER_PATH), MEDIA("MEDIA", MEDIA_PATH), ADMIN("ADMIN", ADMIN_PATH);
    private String role;
    private String path;

    Role(String role, String path) {
        this.role = role;
        this.path = path;
    }

    public String getBuildRole() {
        return "ROLE_" + role;
    }

    public static class Paths {
        public static final String USER_PATH = "/api/user";
        public static final String MEDIA_PATH = "/api/media";
        public static final String ADMIN_PATH = "/api/admin";
    }
}