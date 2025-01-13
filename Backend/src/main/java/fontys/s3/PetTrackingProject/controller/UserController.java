package fontys.s3.PetTrackingProject.controller;

import fontys.s3.PetTrackingProject.domain.User;
import fontys.s3.PetTrackingProject.dto.UserDTO;
import fontys.s3.PetTrackingProject.dto.VideoSourceDTO;
import fontys.s3.PetTrackingProject.service.UserService;
import fontys.s3.PetTrackingProject.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@RequiredArgsConstructor
public class UserController {

    @Autowired
    private JwtUtil jwtUtil;

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.getUserByUsername(username);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("username", user.getUsername());
        responseData.put("email", user.getEmail());

        return ResponseEntity.ok(responseData);
    }

    @PatchMapping("/profile")
    public ResponseEntity<AuthController.AuthResponse> updateProfile(
            @RequestBody UserDTO userDTO,
            Authentication authentication
    ) {
        String currentUsername = authentication.getName();

        User updatedUser = userService.updateUserProfile(currentUsername, userDTO);

        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                updatedUser.getUsername(),
                updatedUser.getPassword(),
                authentication.getAuthorities()
        );

        Long userId = updatedUser.getId();

        String newAccessToken = jwtUtil.generateToken(newAuth, userId);
        String refreshToken   = jwtUtil.generateRefreshToken(newAuth);

        return ResponseEntity.ok(
                new AuthController.AuthResponse(newAccessToken, refreshToken)
        );
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(Authentication authentication) {
        String currentUsername = authentication.getName();
        userService.deleteUser(currentUsername);

        return ResponseEntity.ok().build();
    }



    @GetMapping("/video-source")
    public VideoSourceDTO getVideoSource(Authentication authentication) {
        String username = authentication.getName();
        VideoSourceDTO videoSourceDTO = userService.getVideoSource(username);
        return videoSourceDTO;
    }

    @PostMapping("/video-source")
    public void setVideoSource(@RequestBody VideoSourceDTO videoSourceDTO, Authentication authentication) {
        String username = authentication.getName();
        userService.updateVideoSource(username, videoSourceDTO.getVideoSource());
    }
}