package com.techieprogramming.auth_service.service;

import com.techieprogramming.auth_service.Entity.User;
import com.techieprogramming.auth_service.dto.AuthRequest;
import com.techieprogramming.auth_service.dto.RegisterRequest;
import com.techieprogramming.auth_service.repository.UserRepository;
import com.techieprogramming.auth_service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public void register(RegisterRequest request) {

        User user = new User();

        user.setUsername(request.getUsername());

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setRole("USER");

        userRepository.save(user);
    }

    public String login(AuthRequest request) {

        User user = userRepository.findByUsername(
                request.getUsername()
        ).orElseThrow();

        if(passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {

            return jwtService.generateToken(user.getUsername());
        }

        throw new RuntimeException("Invalid Credentials");
    }
}
