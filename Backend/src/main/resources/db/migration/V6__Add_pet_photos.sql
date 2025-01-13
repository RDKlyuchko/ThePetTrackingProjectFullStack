ALTER TABLE pets
    ADD COLUMN main_photo_url VARCHAR(1024);

CREATE TABLE pet_detection_photos (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      pet_id BIGINT NOT NULL,
                                      photo_url VARCHAR(1024) NOT NULL,
                                      CONSTRAINT fk_pet_detection_photos_pet
                                          FOREIGN KEY (pet_id)
                                              REFERENCES pets(id)
                                              ON DELETE CASCADE
);
