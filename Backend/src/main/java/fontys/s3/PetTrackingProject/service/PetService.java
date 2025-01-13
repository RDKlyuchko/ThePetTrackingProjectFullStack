package fontys.s3.PetTrackingProject.service;

import fontys.s3.PetTrackingProject.domain.Pet;
import fontys.s3.PetTrackingProject.domain.User;
import fontys.s3.PetTrackingProject.dto.PetDTO;
import fontys.s3.PetTrackingProject.model.PetEntity;
import fontys.s3.PetTrackingProject.model.UserEntity;
import fontys.s3.PetTrackingProject.repository.PetRepository;
import fontys.s3.PetTrackingProject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final S3Service s3Service;

    @Value("${python.service.url}")
    private String pythonServiceUrl;

    @Value("${python.service.token:my-super-secret-service-token}")
    private String pythonServiceToken;

    private final RestTemplate restTemplate = new RestTemplate();

    public PetDTO createPet(PetDTO petDTO, String username) {
        Pet pet = mapToDomain(petDTO);
        pet.setOwner(userService.getUserByUsername(username));

        PetEntity petEntity = mapToEntity(pet);
        PetEntity savedEntity = petRepository.save(petEntity);
        pet.setId(savedEntity.getId());

        return mapToDTO(pet);
    }

    public PetDTO createPetWithPhotos(String name,
                                      String type,
                                      String breed,
                                      MultipartFile mainPhoto,
                                      MultipartFile[] detectionPhotos,
                                      String username) {
        Pet pet = new Pet();
        pet.setName(name);
        pet.setType(type);
        pet.setBreed(breed);
        pet.setOwner(userService.getUserByUsername(username));

        if (mainPhoto != null && !mainPhoto.isEmpty()) {
            String mainPhotoFileName = generateFileName(name, mainPhoto.getOriginalFilename(), "main");
            String mainPhotoUrl = s3Service.uploadFile(mainPhoto, mainPhotoFileName);
            pet.setMainPhotoUrl(mainPhotoUrl);
        }

        List<String> detectionPhotoUrls = new ArrayList<>();
        if (detectionPhotos != null) {
            for (MultipartFile photo : detectionPhotos) {
                if (photo != null && !photo.isEmpty()) {
                    String detectionPhotoFileName = generateFileName(name, photo.getOriginalFilename(), "extra");
                    String url = s3Service.uploadFile(photo, detectionPhotoFileName);
                    detectionPhotoUrls.add(url);
                }
            }
        }
        pet.setDetectionPhotoUrls(detectionPhotoUrls);

        PetEntity petEntity = mapToEntity(pet);
        PetEntity savedEntity = petRepository.save(petEntity);
        pet.setId(savedEntity.getId());

        PetDTO createdPetDTO = mapToDTO(pet);

        if (!detectionPhotoUrls.isEmpty()) {
            String userId = getUserIdForUsername(username);
            callPythonAddPet(userId, pet.getId(), detectionPhotoUrls);
        }

        return createdPetDTO;
    }

    private String generateFileName(String petName, String originalFilename, String folder) {
        return String.format("pets/%s/%s/%d_%s",
                petName.replaceAll("\\s+", "_"),
                folder,
                System.currentTimeMillis(),
                originalFilename
        );
    }

    private String getUserIdForUsername(String username) {
        User user = userService.getUserByUsername(username);
        return String.valueOf(user.getId());
    }

    private void callPythonAddPet(String userId, Long petId, List<String> detectionPhotoUrls) {
        String addPetUrl = pythonServiceUrl + "/add_pet";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("token", pythonServiceToken);
        requestBody.put("userId", userId);
        requestBody.put("petId", petId);
        requestBody.put("refImageUrls", detectionPhotoUrls);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(addPetUrl, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Successfully triggered /add_pet in Python");
            } else {
                System.err.println("Call to /add_pet returned status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("Error calling Python /add_pet: " + e.getMessage());
        }
    }

    public List<PetDTO> getUserPets(String username) {
        User user = userService.getUserByUsername(username);
        List<PetEntity> petEntities = petRepository.findByUserId(user.getId());

        return petEntities.stream()
                .map(this::mapToDomain)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public PetDTO getPetById(Long petId, String username) {
        PetEntity petEntity = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Pet not found"));

        if (!petEntity.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized access to pet");
        }

        Pet pet = mapToDomain(petEntity);
        return mapToDTO(pet);
    }

    public PetDTO updatePet(Long petId, PetDTO petDTO, String username) {
        PetEntity petEntity = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Pet not found"));

        if (!petEntity.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized access to pet");
        }

        petEntity.setName(petDTO.getName());
        petEntity.setType(petDTO.getType());
        petEntity.setBreed(petDTO.getBreed());

        PetEntity updated = petRepository.save(petEntity);
        Pet updatedDomain = mapToDomain(updated);
        return mapToDTO(updatedDomain);
    }

    public void deletePet(Long petId, String username) {
        PetEntity petEntity = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Pet not found"));

        if (!petEntity.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized access to pet");
        }

        petRepository.delete(petEntity);
    }

    private Pet mapToDomain(PetDTO dto) {
        Pet pet = new Pet();
        pet.setName(dto.getName());
        pet.setType(dto.getType());
        pet.setBreed(dto.getBreed());
        pet.setId(dto.getId());
        pet.setMainPhotoUrl(dto.getMainPhotoUrl());
        pet.setDetectionPhotoUrls(dto.getDetectionPhotoUrls());
        return pet;
    }

    private Pet mapToDomain(PetEntity entity) {
        Pet pet = new Pet();
        pet.setName(entity.getName());
        pet.setType(entity.getType());
        pet.setBreed(entity.getBreed());
        pet.setId(entity.getId());
        pet.setMainPhotoUrl(entity.getMainPhotoUrl());
        pet.setDetectionPhotoUrls(entity.getDetectionPhotoUrls());
        return pet;
    }

    private PetEntity mapToEntity(Pet pet) {
        PetEntity entity = new PetEntity();
        entity.setId(pet.getId());
        entity.setName(pet.getName());
        entity.setType(pet.getType());
        entity.setBreed(pet.getBreed());
        entity.setMainPhotoUrl(pet.getMainPhotoUrl());
        entity.setDetectionPhotoUrls(pet.getDetectionPhotoUrls());

        if (pet.getOwner() != null && pet.getOwner().getId() != null) {
            UserEntity userEntity = userRepository.findById(pet.getOwner().getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            entity.setUser(userEntity);
        }
        return entity;
    }

    private PetDTO mapToDTO(Pet pet) {
        PetDTO dto = new PetDTO();
        dto.setName(pet.getName());
        dto.setType(pet.getType());
        dto.setBreed(pet.getBreed());
        dto.setId(pet.getId());
        dto.setMainPhotoUrl(pet.getMainPhotoUrl());
        dto.setDetectionPhotoUrls(pet.getDetectionPhotoUrls());
        return dto;
    }
}
