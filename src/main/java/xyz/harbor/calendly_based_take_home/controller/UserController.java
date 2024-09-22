package xyz.harbor.calendly_based_take_home.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import xyz.harbor.calendly_based_take_home.request.CreateUserRequest;
import xyz.harbor.calendly_based_take_home.request.UpdateUserPasswordRequest;
import xyz.harbor.calendly_based_take_home.response.UserResponse;

import org.springframework.beans.factory.annotation.Autowired;
import xyz.harbor.calendly_based_take_home.service.UserService;

@RestController
@RequestMapping(path = "${version}/user")
public class UserController {

    @Autowired
    UserService userService;

    @RequestMapping(value = {"/create", "/create/"}, method = RequestMethod.POST)
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest createUserRequest){
        return ResponseEntity.ok().body(userService.createNewUser(createUserRequest));
    }

    @RequestMapping(value = {"/change-password", "/change-password/"}, method = RequestMethod.POST)
    public ResponseEntity<UserResponse> changePasswordForUser(@RequestBody UpdateUserPasswordRequest updateUserPasswordRequest){
        return ResponseEntity.ok().body(userService.updatePassword(updateUserPasswordRequest));
    }
}
