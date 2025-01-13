package fontys.s3.PetTrackingProject.service;

import fontys.s3.PetTrackingProject.model.DetectionLogEntity;
import fontys.s3.PetTrackingProject.model.PetEntity;
import fontys.s3.PetTrackingProject.repository.DetectionLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DetectionServiceTest {

    @Mock
    private DetectionLogRepository detectionLogRepository;

    @InjectMocks
    private DetectionService detectionService;

    private DetectionLogEntity detectionLog;
    private PetEntity pet;

    @BeforeEach
    void setUp() {
        pet = new PetEntity();
        pet.setId(1L);

        detectionLog = new DetectionLogEntity();
        detectionLog.setId(1L);
        detectionLog.setPet(pet);
    }

    @Test
    void testSaveLog() {
        when(detectionLogRepository.save(detectionLog)).thenReturn(detectionLog);

        DetectionLogEntity savedLog = detectionService.saveLog(detectionLog);

        assertThat(savedLog).isNotNull();
        assertThat(savedLog.getId()).isEqualTo(1L);
        verify(detectionLogRepository, times(1)).save(detectionLog);
    }

    @Test
    void testGetLogsByPet() {
        when(detectionLogRepository.findByPet(pet)).thenReturn(List.of(detectionLog));

        List<DetectionLogEntity> logs = detectionService.getLogsByPet(pet);

        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getId()).isEqualTo(1L);
        verify(detectionLogRepository, times(1)).findByPet(pet);
    }
}