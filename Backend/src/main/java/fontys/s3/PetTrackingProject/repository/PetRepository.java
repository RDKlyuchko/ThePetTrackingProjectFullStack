package fontys.s3.PetTrackingProject.repository;


import fontys.s3.PetTrackingProject.model.PetEntity;
import fontys.s3.PetTrackingProject.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<PetEntity, Long> {
    List<PetEntity> findByUser(UserEntity user);
    List<PetEntity> findByUserId(Long userId);
}