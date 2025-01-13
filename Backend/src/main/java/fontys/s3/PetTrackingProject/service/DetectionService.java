package fontys.s3.PetTrackingProject.service;

import fontys.s3.PetTrackingProject.dto.DetectionLogDTO;
import fontys.s3.PetTrackingProject.model.DetectionLogEntity;
import fontys.s3.PetTrackingProject.model.PetEntity;
import fontys.s3.PetTrackingProject.repository.DetectionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DetectionService {

    private final DetectionLogRepository detectionLogRepository;

    @Autowired
    public DetectionService(DetectionLogRepository detectionLogRepository) {
        this.detectionLogRepository = detectionLogRepository;
    }

    public DetectionLogEntity saveLog(DetectionLogEntity log) {
        return detectionLogRepository.save(log);
    }

    public List<DetectionLogEntity> getLogsByPet(PetEntity pet) {
        return detectionLogRepository.findByPet(pet);
    }

    public List<DetectionLogEntity> getLogsByUserId(Long userId) {
        return detectionLogRepository.findAllLogsByUserId(userId);
    }

    public List<DetectionLogDTO> getLogsByUserIdAsDTO(Long userId) {
        List<DetectionLogEntity> logEntities = detectionLogRepository.findAllLogsByUserId(userId);

        return logEntities.stream()
                .map(this::mapToDTO)
                .toList();
    }

    private DetectionLogDTO mapToDTO(DetectionLogEntity entity) {
        DetectionLogDTO dto = new DetectionLogDTO();
        dto.setId(entity.getId());
        dto.setCurrentState(entity.getCurrentState());
        dto.setTimestamp(entity.getTimestamp());
        PetEntity pet = entity.getPet();
        dto.setPetId(pet.getId());
        dto.setPetName(pet.getName());

        return dto;
    }
}