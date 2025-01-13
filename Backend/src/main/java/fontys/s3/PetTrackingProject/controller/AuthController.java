package fontys.s3.PetTrackingProject.controller;

import fontys.s3.PetTrackingProject.domain.User;
import fontys.s3.PetTrackingProject.dto.UserDTO;
import fontys.s3.PetTrackingProject.service.UserService;
import fontys.s3.PetTrackingProject.util.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private UserDetailsService userDetailsService;

    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> createAuthenticationToken(@RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getUsername(),
                        authRequest.getPassword()
                )
        );

        User userDomain = userService.getUserByUsername(authRequest.getUsername());
        Long userId = userDomain.getId();

        String accessToken = jwtUtil.generateToken(authentication, userId);
        String refreshToken = jwtUtil.generateRefreshToken(authentication);

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        try {
            String username = jwtUtil.extractUsername(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateRefreshToken(refreshToken, userDetails)) {
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );

                User user = userService.getUserByUsername(username);
                Long userId = user.getId();

                String newAccessToken = jwtUtil.generateToken(authentication, userId);

                return ResponseEntity.ok(new AuthResponse(newAccessToken, refreshToken));
            } else {
                return ResponseEntity.status(401).body(null);
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body(null);
        }
    }


    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserDTO userDTO) {
        userService.registerUser(userDTO);

        return ResponseEntity.ok("User registered successfully");
    }

    @Data
    static class AuthRequest {
        private String username;
        private String password;
    }

    @Data
    @AllArgsConstructor
    static class AuthResponse {
        private String accessToken;
        private String refreshToken;
    }

    @Data
    static class RefreshTokenRequest {
        private String refreshToken;
    }
}