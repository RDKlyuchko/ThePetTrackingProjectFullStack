package fontys.s3.PetTrackingProject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetectionLogDTO {
    private Long id;
    private String currentState;
    private LocalDateTime timestamp;
    private String petName;
    private Long petId;

}
