package fontys.s3.PetTrackingProject.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Data
public class PetDTO {

    @NotBlank(message = "Name is mandatory")
    private String name;

    @NotBlank(message = "Type is mandatory")
    private String type;

    @NotBlank(message = "Breed is mandatory")
    private String breed;

    private Long id;
    private String mainPhotoUrl;
    private List<String> detectionPhotoUrls;
}
