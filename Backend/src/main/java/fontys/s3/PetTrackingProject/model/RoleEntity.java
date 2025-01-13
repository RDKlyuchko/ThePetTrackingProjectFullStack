package fontys.s3.PetTrackingProject.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class RoleEntity {

    @Id
    @Column(name = "role_name")
    @NonNull
    private String roleName;

    @ManyToMany(mappedBy = "roles")
    private List<UserEntity> users;
}