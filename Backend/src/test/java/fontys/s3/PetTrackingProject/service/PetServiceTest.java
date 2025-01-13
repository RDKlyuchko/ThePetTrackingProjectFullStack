package fontys.s3.PetTrackingProject.service;

import fontys.s3.PetTrackingProject.domain.User;
import fontys.s3.PetTrackingProject.dto.PetDTO;
import fontys.s3.PetTrackingProject.model.PetEntity;
import fontys.s3.PetTrackingProject.model.UserEntity;
import fontys.s3.PetTrackingProject.repository.PetRepository;
import fontys.s3.PetTrackingProject.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class PetServiceTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private PetService petService;

    private User userDomain;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        userDomain = new User();
        userDomain.setId(1L);
        userDomain.setUsername("john_doe");

        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("john_doe");
    }

    @Test
    void testCreatePet() {
        // Arrange
        PetDTO petDTO = new PetDTO();
        petDTO.setName("Buddy");
        petDTO.setType("Dog");
        petDTO.setBreed("Labrador");

        when(userService.getUserByUsername("john_doe")).thenReturn(userDomain);
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));

        PetEntity savedPetEntity = new PetEntity();
        savedPetEntity.setId(10L);
        savedPetEntity.setName("Buddy");
        savedPetEntity.setType("Dog");
        savedPetEntity.setBreed("Labrador");
        savedPetEntity.setUser(userEntity);

        when(petRepository.save(any(PetEntity.class))).thenAnswer(invocation -> {
            PetEntity entity = invocation.getArgument(0);
            entity.setId(10L);
            return entity;
        });

        // Act
        PetDTO createdPet = petService.createPet(petDTO, "john_doe");

        // Assert
        assertThat(createdPet).isNotNull();
        assertThat(createdPet.getId()).isEqualTo(10L);
        assertThat(createdPet.getName()).isEqualTo("Buddy");
        assertThat(createdPet.getType()).isEqualTo("Dog");
        assertThat(createdPet.getBreed()).isEqualTo("Labrador");

        verify(userService).getUserByUsername("john_doe");
        verify(userRepository).findById(1L);
        verify(petRepository).save(any(PetEntity.class));
    }

    @Test
    void testGetUserPets() {
        when(userService.getUserByUsername("john_doe")).thenReturn(userDomain);

        PetEntity petEntity = new PetEntity();
        petEntity.setId(1L);
        petEntity.setName("Buddy");
        petEntity.setType("Dog");
        petEntity.setBreed("Labrador");
        petEntity.setUser(userEntity);

        when(petRepository.findByUserId(1L)).thenReturn(List.of(petEntity));

        List<PetDTO> userPets = petService.getUserPets("john_doe");

        assertThat(userPets).hasSize(1);
        PetDTO petDTO = userPets.get(0);
        assertThat(petDTO.getId()).isEqualTo(1L);
        assertThat(petDTO.getName()).isEqualTo("Buddy");
        assertThat(petDTO.getType()).isEqualTo("Dog");
        assertThat(petDTO.getBreed()).isEqualTo("Labrador");

        verify(userService).getUserByUsername("john_doe");
        verify(petRepository).findByUserId(1L);
    }

    @Test
    void testCreatePet_UserNotFound() {
        PetDTO petDTO = new PetDTO();
        petDTO.setName("Buddy");
        petDTO.setType("Dog");
        petDTO.setBreed("Labrador");

        when(userService.getUserByUsername("unknown_user"))
                .thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, () -> {
            petService.createPet(petDTO, "unknown_user");
        });

        verify(userService).getUserByUsername("unknown_user");
        verifyNoInteractions(petRepository);
    }
}