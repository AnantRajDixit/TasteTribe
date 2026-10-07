package com.tastetribe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * TasteTribe — Recipe Sharing Platform.
 *
 * <p>Entry point of the Spring Boot application. The servlet container (Tomcat)
 * starts on port 8080 with a {@code /api} context path; the React frontend calls
 * every endpoint through a relative {@code /api} path.</p>
 */
@SpringBootApplication
public class TasteTribeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TasteTribeApplication.class, args);
    }
}
