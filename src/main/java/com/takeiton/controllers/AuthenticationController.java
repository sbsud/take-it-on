package com.takeiton.controllers;

import com.takeiton.models.AppUser;
import com.takeiton.models.AuthenticationRequest;
import com.takeiton.models.AuthenticationResponse;
import com.takeiton.repositories.AppUserRepository;
import com.takeiton.services.AppUserDetailsService;
import com.takeiton.services.TokenService;
import com.takeiton.util.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AppUserDetailsService userDetailsService;

    @Autowired
    private AppUserRepository applicationUserRepository;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private TokenService tokenService;

    @PostMapping(value = "/api/signup")
    public ResponseEntity<?> signup(@Validated @RequestBody AppUser user) {
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        applicationUserRepository.save(user);
        return ResponseEntity.ok("Successfully signed up.");
    }

    @PostMapping(value = "/api/login")
    public ResponseEntity<?> createAuthToken(@RequestBody AuthenticationRequest authRequest) throws Exception {

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        } catch (BadCredentialsException e) {
            throw new Exception("Incorrect username or password", e);
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());

        String jwt = jwtUtil.generateToken(userDetails);

        tokenService.save(authRequest.getUsername(), jwt);

        return ResponseEntity.ok(new AuthenticationResponse(jwt));
    }

    @PostMapping(value = "/api/logout")
    public ResponseEntity<?> deleteAuthToken(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");

        String username = jwtUtil.getUsernameFromAuthorizationHeader(authHeader);
        tokenService.delete(username);

        return ResponseEntity.ok("Successfully logged out.");
    }
}
