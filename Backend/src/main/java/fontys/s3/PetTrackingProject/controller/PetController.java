package fontys.s3.PetTrackingProject.controller;

import fontys.s3.PetTrackingProject.dto.PetDTO;
import fontys.s3.PetTrackingProject.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @PostMapping
    public ResponseEntity<PetDTO> createPet(@Valid @RequestBody PetDTO petDTO, Authentication authentication) {
        String username = authentication.getName();
        PetDTO createdPetDTO = petService.createPet(petDTO, username);
        return ResponseEntity.ok(createdPetDTO);
    }

    @GetMapping
    public ResponseEntity<List<PetDTO>> getUserPets(Authentication authentication) {
        String username = authentication.getName();
        List<PetDTO> userPets = petService.getUserPets(username);
        return ResponseEntity.ok(userPets);
    }
    @PostMapping(value = "/create-with-photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PetDTO> createPetWithPhotos(
            @RequestParam("name") String name,
            @RequestParam("type") String type,
            @RequestParam("breed") String breed,
            @RequestPart("mainPhoto") MultipartFile mainPhoto,
            @RequestPart("detectionPhotos") MultipartFile[] detectionPhotos,
            Authentication authentication
    ) {
        String username = authentication.getName();

        PetDTO created = petService.createPetWithPhotos(
                name, type, breed,
                mainPhoto, detectionPhotos,
                username
        );

        return ResponseEntity.ok(created);
    }

    @GetMapping("/{petId}")
    public ResponseEntity<PetDTO> getPetById(@PathVariable Long petId,
                                             Authentication authentication) {
        String username = authentication.getName();
        PetDTO petDTO = petService.getPetById(petId, username);
        return ResponseEntity.ok(petDTO);
    }

    @PutMapping("/{petId}")
    public ResponseEntity<PetDTO> updatePet(@PathVariable Long petId,
                                            @Valid @RequestBody PetDTO petDTO,
                                            Authentication authentication) {
        String username = authentication.getName();
        PetDTO updatedPet = petService.updatePet(petId, petDTO, username);
        return ResponseEntity.ok(updatedPet);
    }

    @DeleteMapping("/{petId}")
    public ResponseEntity<Void> deletePet(@PathVariable Long petId,
                                          Authentication authentication) {
        String username = authentication.getName();
        petService.deletePet(petId, username);
        return ResponseEntity.noContent().build();
    }
}
