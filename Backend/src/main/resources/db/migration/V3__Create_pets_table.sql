CREATE TABLE pets (
                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      name VARCHAR(255) NOT NULL,
                      type VARCHAR(255) NOT NULL,
                      breed VARCHAR(255) NOT NULL,
                      user_id BIGINT NOT NULL,
                      CONSTRAINT fk_pets_user FOREIGN KEY (user_id) REFERENCES users(id)
);