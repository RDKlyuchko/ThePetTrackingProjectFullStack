package fontys.s3.PetTrackingProject.controller;

import fontys.s3.PetTrackingProject.dto.DetectionLogDTO;
import fontys.s3.PetTrackingProject.model.DetectionLogEntity;
import fontys.s3.PetTrackingProject.model.PetEntity;
import fontys.s3.PetTrackingProject.repository.PetRepository;
import fontys.s3.PetTrackingProject.service.DetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;


import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/logs")
public class DetectionController {

    private final DetectionService detectionService;
    private final PetRepository petRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public DetectionController(DetectionService detectionService, PetRepository petRepository, SimpMessagingTemplate simpMessagingTemplate) {
        this.detectionService = detectionService;
        this.petRepository = petRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @PostMapping
    public ResponseEntity<String> saveLog(@RequestBody LogRequest request) {
        Optional<PetEntity> petOptional = petRepository.findById(request.getPetId());
        if (petOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid pet ID");
        }

        PetEntity pet = petOptional.get();
        String ownerUsername = pet.getUser().getUsername();

        Long ownerId = pet.getUser().getId();

        DetectionLogEntity log = new DetectionLogEntity();
        log.setPreviousState(request.getPreviousState());
        log.setCurrentState(request.getCurrentState());
        log.setTimestamp(LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getTimestamp()), ZoneId.systemDefault()));
        log.setPet(pet);

        detectionService.saveLog(log);

        simpMessagingTemplate.convertAndSend("/topic/user_" + ownerId + "_logs", log);
        return ResponseEntity.ok("Log saved and sent to user " + ownerUsername);
    }

    @GetMapping("/{petId}")
    public ResponseEntity<?> getLogsByPetId(@PathVariable Long petId) {
        Optional<PetEntity> petOptional = petRepository.findById(petId);
        if (petOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid pet ID");
        }

        PetEntity pet = petOptional.get();
        var logs = detectionService.getLogsByPet(pet);

        return ResponseEntity.ok(logs);
    }

    @GetMapping("/user/{userId}/all")
    public ResponseEntity<?> getAllLogsByUserId(@PathVariable Long userId) {
        List<DetectionLogDTO> logs = detectionService.getLogsByUserIdAsDTO(userId);

        if (logs.isEmpty()) {
            return ResponseEntity.ok("No logs found for user with id " + userId);
        }
        return ResponseEntity.ok(logs);
    }

    public static class LogRequest {
        private String previousState;
        private String currentState;
        private long timestamp;
        private Long petId;

        public String getPreviousState() {
            return previousState;
        }

        public void setPreviousState(String previousState) {
            this.previousState = previousState;
        }

        public String getCurrentState() {
            return currentState;
        }

        public void setCurrentState(String currentState) {
            this.currentState = currentState;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public Long getPetId() {
            return petId;
        }

        public void setPetId(Long petId) {
            this.petId = petId;
        }
    }
}