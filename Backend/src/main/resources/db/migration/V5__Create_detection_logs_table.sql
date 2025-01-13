CREATE TABLE detection_logs (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          previous_state VARCHAR(255) NOT NULL,
                          current_state VARCHAR(255) NOT NULL,
                          timestamp DATETIME NOT NULL,
                          pet_id BIGINT NOT NULL,
                          FOREIGN KEY (pet_id) REFERENCES pets(id) ON DELETE CASCADE
);