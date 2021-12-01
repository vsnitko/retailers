package com.itechart.retailers.controller;

import com.itechart.retailers.model.entity.Role;
import com.itechart.retailers.model.entity.User;
import com.itechart.retailers.model.payload.request.SignUpRequest;
import com.itechart.retailers.model.payload.response.MessageResponse;
import com.itechart.retailers.repository.RoleRepository;
import com.itechart.retailers.repository.UserRepository;
import com.itechart.retailers.service.RoleService;
import com.itechart.retailers.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system-admin")
@RequiredArgsConstructor
public class SystemAdminController {

    private final UserService userService;
    private final RoleService roleService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @PostMapping
    public ResponseEntity<?> registerUser(@RequestBody SignUpRequest signUpRequest) {
        if (userService.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }
        Role role = roleService.save(Role.builder()
                .role("RETAIL_ADMIN")
                .build());
        User user = User.builder()
                .name(signUpRequest.getName())
                .email(signUpRequest.getEmail())
                .role(role)
                .password(passwordEncoder.encode("1111"))
                .isActive(true)
                .build();
        userService.save(user);
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}