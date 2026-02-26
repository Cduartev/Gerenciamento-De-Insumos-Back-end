package com.project.inventory.controller;

import com.project.inventory.domain.entity.User;
import com.project.inventory.domain.repository.UserRepository;
import com.project.inventory.dto.LoginRequestDTO;
import com.project.inventory.dto.LoginResponseDTO;
import com.project.inventory.dto.RegisterRequestDTO;
import com.project.inventory.security.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        User user = (User) auth.getPrincipal();
        var token = tokenService.generateToken(user);

        return ResponseEntity.ok(new LoginResponseDTO(token, user.getName(), user.getEmail(), user.getRole().name()));
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterRequestDTO data) {
        if (this.userRepository.findByEmail(data.email()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        String encryptedPassword = passwordEncoder.encode(data.password());
        User newUser = User.builder()
                .name(data.name())
                .email(data.email())
                .password(encryptedPassword)
                .role(data.role())
                .build();
        
        this.userRepository.save(newUser);

        return ResponseEntity.ok().build();
    }
}
