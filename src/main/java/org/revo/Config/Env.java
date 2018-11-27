package org.revo.Config;

import lombok.Getter;
import lombok.Setter;
import org.revo.Domain.Bucket;
import org.revo.Domain.User;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ashraf on 17/04/17.
 */
@Getter
@Setter
@ConfigurationProperties("org.revo.env")
public class Env {
    private String schema;
    private Route eureka;
    private Route config;
    private List<User> users = new ArrayList<>();
    private Map<String, Bucket> buckets = new HashMap<>();
}
