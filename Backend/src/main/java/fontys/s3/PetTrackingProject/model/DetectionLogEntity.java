package fontys.s3.PetTrackingProject.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "detection_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetectionLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "previous_state", nullable = false)
    private String previousState;

    @Column(name = "current_state", nullable = false)
    private String currentState;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private PetEntity pet;
}