package com.auth.authbackend.service.impl;

import com.auth.authbackend.dto.UserDto;
import com.auth.authbackend.service.AuthService;
import com.auth.authbackend.service.UserService;
import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;

    @Override
    public UserDto registerUser(UserDto userDto) {
        //logic
        //verify email
        //verify password
        //default roles

        return userService.createUser(userDto);
    }
}
