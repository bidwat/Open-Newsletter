package com.example.core_service.auth0;

import com.example.core_service.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.client.RestClient;

@Service
public class Auth0UserInfoService {

    private final RestClient client;

    public Auth0UserInfoService(@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuer) {
        String base = issuer != null && issuer.endsWith("/")
                ? issuer.substring(0, issuer.length() - 1)
                : issuer;
        this.client = RestClient.builder().baseUrl(base).build();
    }

    public UserInfoResponse fetchUserInfo(String accessToken) {

        return client.get()
                .uri("/userinfo")
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .body(UserInfoResponse.class);
    }

}






