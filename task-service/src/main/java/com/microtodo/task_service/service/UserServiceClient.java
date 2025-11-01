package com.microtodo.task_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.microtodo.task_service.dto.UserDto;

@Service
public class UserServiceClient {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${user.service.url}")
    private String userServiceUrl;
    
    public UserDto getUserById(Long userId) {
        try {
            ResponseEntity<UserDto> response = restTemplate.getForEntity(
                userServiceUrl + "/api/users/" + userId, 
                UserDto.class
            );
            return response.getBody();
        } catch (RestClientException e) {
            throw new RuntimeException("User not found with id: " + userId, e);
        }
    }
}

