package fontys.s3.PetTrackingProject.service;

import fontys.s3.PetTrackingProject.domain.User;
import fontys.s3.PetTrackingProject.dto.UserDTO;
import fontys.s3.PetTrackingProject.dto.VideoSourceDTO;
import fontys.s3.PetTrackingProject.model.RoleEntity;
import fontys.s3.PetTrackingProject.model.UserEntity;
import fontys.s3.PetTrackingProject.repository.RoleRepository;
import fontys.s3.PetTrackingProject.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    public void testRegisterUser() {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testuser");
        userDTO.setPassword("password");
        userDTO.setEmail("test@example.com");

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findById(anyString())).thenReturn(Optional.empty());
        when(roleRepository.save(any(RoleEntity.class))).thenAnswer(invocation -> {
            RoleEntity roleEntity = invocation.getArgument(0);
            return roleEntity;
        });

        ArgumentCaptor<UserEntity> userEntityCaptor = ArgumentCaptor.forClass(UserEntity.class);

        // Act
        userService.registerUser(userDTO);

        // Assert
        verify(userRepository, times(1)).save(userEntityCaptor.capture());
        UserEntity savedUserEntity = userEntityCaptor.getValue();

        assertEquals("testuser", savedUserEntity.getUsername());
        assertEquals("encodedPassword", savedUserEntity.getPassword());
        assertEquals("test@example.com", savedUserEntity.getEmail());
        assertNotNull(savedUserEntity.getRoles());
        assertEquals(1, savedUserEntity.getRoles().size());
        assertEquals("USER", savedUserEntity.getRoles().get(0).getRoleName());
    }

    @Test
    public void testRegisterUser_RoleExists() {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testuser");
        userDTO.setPassword("password");
        userDTO.setEmail("test@example.com");

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        RoleEntity existingRole = new RoleEntity();
        existingRole.setRoleName("USER");

        when(roleRepository.findById("USER")).thenReturn(Optional.of(existingRole));

        ArgumentCaptor<UserEntity> userEntityCaptor = ArgumentCaptor.forClass(UserEntity.class);

        // Act
        userService.registerUser(userDTO);

        // Assert
        verify(userRepository, times(1)).save(userEntityCaptor.capture());
        UserEntity savedUserEntity = userEntityCaptor.getValue();

        assertEquals("testuser", savedUserEntity.getUsername());
        assertEquals("encodedPassword", savedUserEntity.getPassword());
        assertEquals("test@example.com", savedUserEntity.getEmail());
        assertNotNull(savedUserEntity.getRoles());
        assertEquals(1, savedUserEntity.getRoles().size());
        assertEquals("USER", savedUserEntity.getRoles().get(0).getRoleName());
    }

    @Test
    public void testGetVideoSource() {
        // Arrange
        String username = "testuser";
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setVideoSource("video_source_url");

        userEntity.setRoles(new ArrayList<>());

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));

        // Act
        VideoSourceDTO videoSourceDTO = userService.getVideoSource(username);

        // Assert
        assertNotNull(videoSourceDTO);
        assertEquals("video_source_url", videoSourceDTO.getVideoSource());
    }

    @Test
    public void testUpdateVideoSource() {
        // Arrange
        String username = "testuser";
        String newVideoSource = "new_video_source_url";

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setVideoSource("old_video_source_url");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));

        // Act
        userService.updateVideoSource(username, newVideoSource);

        // Assert
        assertEquals(newVideoSource, userEntity.getVideoSource());
        verify(userRepository, times(1)).save(userEntity);
    }

    @Test
    public void testUpdateVideoSource_UserNotFound() {
        // Arrange
        String username = "nonexistentuser";
        String newVideoSource = "new_video_source_url";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.updateVideoSource(username, newVideoSource);
        });

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    public void testGetUserByUsername() {
        // Arrange
        String username = "testuser";
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername(username);
        userEntity.setEmail("test@example.com");
        userEntity.setVideoSource("video_source_url");

        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setRoleName("USER");
        userEntity.setRoles(List.of(roleEntity));

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));

        // Act
        User user = userService.getUserByUsername(username);

        // Assert
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals(username, user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("video_source_url", user.getVideoSource());
        assertNotNull(user.getRoles());
        assertEquals(1, user.getRoles().size());
        assertEquals("USER", user.getRoles().get(0).getRoleName());
    }

    @Test
    public void testGetUserByUsername_UserNotFound() {
        // Arrange
        String username = "nonexistentuser";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.getUserByUsername(username);
        });

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    public void testLoadUserByUsername() {
        // Arrange
        String username = "testuser";
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setPassword("encodedPassword");

        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setRoleName("USER");
        userEntity.setRoles(List.of(roleEntity));

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));

        // Act
        UserDetails userDetails = userService.loadUserByUsername(username);

        // Assert
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }

    @Test
    public void testLoadUserByUsername_UserNotFound() {
        // Arrange
        String username = "nonexistentuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername(username);
        });
    }
}