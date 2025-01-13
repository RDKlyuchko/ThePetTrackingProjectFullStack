plugins {
	java
	id("org.springframework.boot") version "3.3.3"
	id("io.spring.dependency-management") version "1.1.6"
	id("org.sonarqube") version "4.3.0.3225"
	id("jacoco")
}

group = "fontys.s3"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(17))
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// Spring Boot Starters
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-websocket")

	// Amazon S3 Bucket
	implementation("com.amazonaws:aws-java-sdk-s3:1.12.526")

	// Lombok
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	// MySQL Connector
	implementation("org.flywaydb:flyway-mysql")
	implementation("mysql:mysql-connector-java:8.0.33")

	// JWT Dependencies (Added)
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

	// Jakarta Persistence API (Added)
	implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")

	// Testing Dependencies
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("com.h2database:h2")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}

tasks.withType<Test> {
	useJUnitPlatform()
}

sonarqube {
	properties {
		property("sonar.projectKey", "PetTrackingProject")
		property("sonar.projectName", "PetTrackingProject")
		property("sonar.host.url", "http://host.docker.internal:9000")
		property("sonar.login", "squ_9e3d94485bf198c1172f49c2f6af1cba153e809e")

		property("sonar.java.coveragePlugin", "jacoco")
		property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/xml/report.xml")

		property("sonar.inclusions", "src/main/java/fontys/s3/PetTrackingProject/service/**")

		property("sonar.exclusions", "src/main/java/fontys/s3/PetTrackingProject/controller/**,src/main/java/fontys/s3/PetTrackingProject/repository/**")
	}
}

jacoco {
	toolVersion = "0.8.10"
}

tasks.test {
	useJUnitPlatform()
	finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required.set(true)
		csv.required.set(false)
		html.required.set(true)
	}
}