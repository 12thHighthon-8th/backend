group = "com.peugether"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
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

plugins {
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.spring") version "2.1.0"
    kotlin("plugin.jpa") version "2.1.0"      // JPA entity용 no-arg 생성자
    kotlin("kapt") version "2.1.0"            // 어노테이션 프로세서
}

dependencies {
    // ── Core ──
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // ── Database & Cache ──
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // ── Security & JWT ──
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // ── 외부 서비스 연동 ──
    implementation("org.springframework.boot:spring-boot-starter-webflux")  // WebClient
    implementation("com.google.firebase:firebase-admin:9.4.1")              // FCM

    // ★ MacOS용 Netty DNS 네이티브 라이브러리 (Apple Silicon 기준)
    runtimeOnly("io.netty:netty-resolver-dns-native-macos:4.1.118.Final:osx-aarch_64")

    // ── API 문서화 ──
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.4")

    // ── 개발 도구 ──
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // ── 테스트 ──
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.mockk:mockk:1.13.14")
    testImplementation("com.h2database:h2")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
