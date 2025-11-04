package com.example.demo.config.auth.provider;

import java.util.Map;

public interface OAuth2UserInfo {
    String getName();
    String getEmail();
    String getProvider();
    String getProviderId();
    Map<String, Object> getAttributes();
}
