package fontys.s3.PetTrackingProject.repository;

import fontys.s3.PetTrackingProject.model.DetectionLogEntity;
import fontys.s3.PetTrackingProject.model.PetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetectionLogRepository extends JpaRepository<DetectionLogEntity, Long> {
    List<DetectionLogEntity> findByPet(PetEntity pet);

    @Query("""
    SELECT detectionlog
    FROM DetectionLogEntity detectionlog 
    INNER JOIN detectionlog.pet pet 
    INNER JOIN pet.user user 
    WHERE user.id = :userId
""")
    List<DetectionLogEntity> findAllLogsByUserId(@Param("userId") Long userId);
}