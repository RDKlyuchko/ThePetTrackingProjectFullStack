package fontys.s3.PetTrackingProject.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pet {
    private Long id;
    private String name;
    private String type;
    private String breed;
    private User owner;
    private String mainPhotoUrl;
    private List<String> detectionPhotoUrls;
}
