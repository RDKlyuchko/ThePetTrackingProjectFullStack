package fontys.s3.PetTrackingProject.repository;

import fontys.s3.PetTrackingProject.model.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, String> {
}