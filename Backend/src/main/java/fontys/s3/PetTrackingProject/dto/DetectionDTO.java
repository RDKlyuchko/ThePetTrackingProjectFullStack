package fontys.s3.PetTrackingProject.dto;

import lombok.Data;

@Data
public class DetectionDTO {
    private String dogState;
    private boolean dogDetected;
    private Integer[] dogCenter;
}