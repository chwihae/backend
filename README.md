# Chwihae backend

### Feature

<img src="https://github.com/chwihae/frontend/assets/90189513/7e525a0c-d517-4dfb-b87c-4b2fb3200cdd" alt="취해 스플래쉬 및 로그인 gif 화면" width="400px"/> 

<img src="https://github.com/chwihae/frontend/assets/90189513/3d5078bc-1ba7-424c-8482-131ebd862f53" alt="취해 메인홈의 탭, 필터, 무한스크롤 gif 화면" width="400px"/> 

<img src="https://github.com/chwihae/frontend/assets/90189513/9209e2b8-8698-4323-a1da-8bf80a2f2018" alt="취해 글작성 gif 화면" width="400px"/>

<img src="https://github.com/chwihae/frontend/assets/90189513/2bf7d426-cb33-49bf-98a9-1ed41384ce96" alt="취해 내가쓴투표글 진행중, 마감, 삭제 gif 화면" width="400px"/>

<img src="https://github.com/chwihae/frontend/assets/90189513/a2117536-f8b9-4550-8370-5a2606c5dcfa" alt="취해 타인이쓴투표글 진행중, 댓글 gif 화면" width="400px"/>

### Intro

- Gradle mono module project
- Monolithic Structure
- 3-layer(presentation,logic,data) Structure

### Tech

- Backend
    - Java 17, SpringBoot 3.1.3, Gradle 8.2.1
    - Spring Batch, JPA with QueryDSL, Spring Security
    - JUnit5, Mockito
- Data
    - MariaDB, Redis
- Infra
    - Docker, Github actions, AWS EC2 with DB (Use EC2 with EB instead of ECS for cost and management issues)

### Modules

```groovy
dependencies {

    //springboot
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-security'

    //spring-batch
    implementation 'org.springframework.boot:spring-boot-starter-batch'
    implementation 'org.springframework.batch:spring-batch-integration'

    //spring-cloud
    implementation 'org.springframework.cloud:spring-cloud-starter-openfeign' // For @FeignClient

    //data
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    runtimeOnly 'org.mariadb.jdbc:mariadb-java-client'

    //querydsl
    implementation 'com.querydsl:querydsl-jpa:5.0.0:jakarta'
    annotationProcessor "com.querydsl:querydsl-apt:${dependencyManagement.importedProperties['querydsl.version']}:jakarta"
    annotationProcessor "jakarta.annotation:jakarta.annotation-api"
    annotationProcessor "jakarta.persistence:jakarta.persistence-api"

    //jwt
    implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
    implementation 'io.jsonwebtoken:jjwt-impl:0.11.5'
    implementation 'io.jsonwebtoken:jjwt-jackson:0.11.5'

    //lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    testCompileOnly 'org.projectlombok:lombok'
    testAnnotationProcessor 'org.projectlombok:lombok'

    //testcontainers
    testImplementation "org.testcontainers:testcontainers:1.19.0"
    testImplementation "org.testcontainers:junit-jupiter:1.19.0"
    testImplementation "org.testcontainers:mariadb:1.19.0"

    //test
    testImplementation 'org.springframework.cloud:spring-cloud-contract-wiremock' // For @AutoConfigureWireMock
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
    testImplementation 'org.springframework.batch:spring-batch-test'

    //restdocs
    asciidoctorExt 'org.springframework.restdocs:spring-restdocs-asciidoctor'
    testImplementation 'org.springframework.restdocs:spring-restdocs-mockmvc'
}
```

### Build

0. Requirement
    - Docker Engine
    - Java 17 or higher

1. Before build and run application, you should set up database with the information below

    - Jdbc url: `jdbc:mariadb://localhost:3306/chwihae`
    - Username: `root`
    - Password: `1234`

2. After you set database
    - create schema named `chwihae`
    - and run `./database/service-domain-create.sql` in `chwihae`
    - and run `./database/spring-batch-create.sql` in `chwihae`

3. After you set schema, run command below in your terminal

  ```shell
  ./gradlew clean build && java -jar build/libs/*.jar
  ```

### External APIs in use

The following APIs are being invoked for Kakao OAuth2 authentication

- `POST https://kauth.kakao.com/oauth/token`
- `GET https://kapi.kakao.com/v2/user/me`

> Ref: https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#request-token

### ERD

![](images/erd%20diagram.jpg)

### AWS Architecture

![](https://velog.velcdn.com/images/dragonappear/post/5414caba-fd0d-4efc-ba68-c4822af021c7/image.png)

### Git flow

```shell
|-- master
| |-- hotfix
| | |-- #885
|-- develop
| |-- feat
| | |-- #883
| | |-- #884
| |-- refactor
| | |-- #887
```
