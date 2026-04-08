package com.feedback.system.service;

import com.feedback.system.dto.JwtResponse;
import com.feedback.system.dto.LoginRequest;
import com.feedback.system.dto.SignupRequest;
import com.feedback.system.entity.User;
import com.feedback.system.repository.UserRepository;
import com.feedback.system.security.JwtUtils;
import com.feedback.system.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    public void registerUser(SignupRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        User user = new User();
        user.setName(signupRequest.getName());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setRole(signupRequest.getRole() != null ? signupRequest.getRole() : User.Role.STUDENT);

        userRepository.save(user);
    }

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        System.out.println("--- DEBUG: LOGIN REQUEST RECEIVED FOR EMAIL: " + loginRequest.getEmail());
        
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Error: User not found with email: " + loginRequest.getEmail()));

        String rawPassword = loginRequest.getPassword();
        String storedPassword = user.getPassword();

        // 1. Try BCrypt match
        boolean matches = passwordEncoder.matches(rawPassword, storedPassword);

        // 2. Fallback to Plain Text match (if stored password is not already BCrypt)
        if (!matches && rawPassword.equals(storedPassword)) {
            System.out.println("--- DEBUG: PLAIN TEXT MATCH FOUND. MIGRATING TO BCRYPT...");
            user.setPassword(passwordEncoder.encode(rawPassword));
            userRepository.saveAndFlush(user);
            matches = true;
        }

        if (!matches) {
            System.out.println("--- DEBUG: PASSWORD MISMATCH FOR EMAIL: " + loginRequest.getEmail());
            throw new RuntimeException("Error: Invalid credentials");
        }

        // 3. Authenticate with Security Manager to set context and generate JWT
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), rawPassword));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String role = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .findFirst()
                .orElse("ROLE_STUDENT");

        return new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getName(),
                userDetails.getEmail(),
                role);
    }

    public java.util.List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
