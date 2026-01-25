package org.tribenet.tribenet.controller;

import java.net.http.HttpResponse;
import java.nio.file.attribute.UserPrincipal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tribenet.tribenet.model.User;
import org.tribenet.tribenet.service.UserService;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    UserController(UserService userService){
        this.userService = userService;
    }
    
    @GetMapping()
    public ResponseEntity<List<User>> getAllUsers(Authentication auth){
        List<User> result= userService.getAllUsers(auth);
        return ResponseEntity.ok(result);
    }
}
