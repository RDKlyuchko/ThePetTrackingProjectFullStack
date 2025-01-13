package fontys.s3.PetTrackingProject.service;

import fontys.s3.PetTrackingProject.domain.Role;
import fontys.s3.PetTrackingProject.domain.User;
import fontys.s3.PetTrackingProject.dto.UserDTO;
import fontys.s3.PetTrackingProject.dto.VideoSourceDTO;
import fontys.s3.PetTrackingProject.model.RoleEntity;
import fontys.s3.PetTrackingProject.model.UserEntity;
import fontys.s3.PetTrackingProject.repository.RoleRepository;
import fontys.s3.PetTrackingProject.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public void registerUser(UserDTO userDTO) {

        User user = mapToDomain(userDTO);

        Role userRole = new Role("USER");
        user.setRoles(List.of(userRole));
        UserEntity userEntity = mapToEntity(user);
        userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword()));
        userRepository.save(userEntity);
    }

    public VideoSourceDTO getVideoSource(String username) {
        User user = getUserByUsername(username);
        VideoSourceDTO videoSourceDTO = new VideoSourceDTO();
        videoSourceDTO.setVideoSource(user.getVideoSource());
        return videoSourceDTO;
    }

    public void updateVideoSource(String username, String videoSource) {
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userEntity.setVideoSource(videoSource);
        userRepository.save(userEntity);
    }

    public User getUserByUsername(String username) {
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToDomain(userEntity);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new org.springframework.security.core.userdetails.User(
                userEntity.getUsername(),
                userEntity.getPassword(),
                userEntity.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName()))
                        .collect(Collectors.toList())
        );
    }

    public User updateUserProfile(String currentUsername, UserDTO userDTO) {
        UserEntity userEntity = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (userDTO.getUsername() != null && !userDTO.getUsername().isEmpty()) {
            userEntity.setUsername(userDTO.getUsername());
        }
        if (userDTO.getEmail() != null && !userDTO.getEmail().isEmpty()) {
            userEntity.setEmail(userDTO.getEmail());
        }
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            userEntity.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        userRepository.save(userEntity);

        return mapToDomain(userEntity);
    }

    public void deleteUser(String currentUsername) {
        UserEntity userEntity = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.delete(userEntity);
    }

    private User mapToDomain(UserEntity entity) {
        if (entity == null) return null;
        User user = new User();
        user.setId(entity.getId());
        user.setUsername(entity.getUsername());
        user.setEmail(entity.getEmail());
        user.setVideoSource(entity.getVideoSource());
        user.setRoles(entity.getRoles().stream()
                .map(roleEntity -> new Role(roleEntity.getRoleName()))
                .collect(Collectors.toList()));
        return user;
    }

    private User mapToDomain(UserDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setEmail(dto.getEmail());
        return user;
    }

    private UserEntity mapToEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        entity.setUsername(user.getUsername());
        entity.setPassword(user.getPassword());
        entity.setEmail(user.getEmail());
        entity.setVideoSource(user.getVideoSource());
        entity.setRoles(user.getRoles().stream()
                .map(role -> roleRepository.findById(role.getRoleName())
                        .orElseGet(() -> roleRepository.save(new RoleEntity(role.getRoleName()))))
                .collect(Collectors.toList()));
        return entity;
    }
}