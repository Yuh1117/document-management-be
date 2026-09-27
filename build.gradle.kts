plugins {
	java
	id("org.springframework.boot") version "3.5.3"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.vpgh"
version = "0.0.1-SNAPSHOT"
description = "DMS project"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
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
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-web")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-amqp")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

	implementation("org.postgresql:postgresql:42.7.7")

	implementation("org.flywaydb:flyway-core:11.10.2")
	runtimeOnly("org.flywaydb:flyway-database-postgresql:11.10.2")

	implementation("com.cloudinary:cloudinary-http44:1.39.0")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")
	implementation("software.amazon.awssdk:s3:2.32.9")
	implementation("com.google.api-client:google-api-client:2.8.0")
	implementation("commons-io:commons-io:2.20.0")
	implementation("com.itextpdf:itext-core:9.2.0")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
