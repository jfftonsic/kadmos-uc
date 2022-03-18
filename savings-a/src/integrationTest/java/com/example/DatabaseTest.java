package com.example;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.FileSystemResourceAccessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;

@Testcontainers
public class DatabaseTest {

    // will be started before and stopped after each test method
    @Container
    private PostgreSQLContainer postgresqlContainer = new PostgreSQLContainer("postgres")
            .withDatabaseName("postgres")
            .withUsername("postgres")
            .withPassword("a");
    private static Connection conn;
    private static Liquibase liquibase;

    @BeforeEach
    public void setUp() throws Exception {
        Class.forName("org.postgresql.Driver");

        conn = DriverManager.getConnection(postgresqlContainer.getJdbcUrl()/*"jdbc:postgresql://localhost:5432/"*/,
                postgresqlContainer.getUsername(),
                postgresqlContainer.getPassword());

        Database database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(conn));

        String currentDirectory = System.getProperty("user.dir");
        System.out.println("The current working directory is " + currentDirectory);
        System.out.println("The file exists? " + Files.exists(Paths.get("./src/main/resources/db/changelog/db.changelog-master.yaml")));
        liquibase = new Liquibase("./src/main/resources/db/changelog/db.changelog-master.yaml",
                new FileSystemResourceAccessor(Paths.get(currentDirectory).toFile()), database);
        liquibase.update((Contexts) null);

        System.out.println();
    }

    @Test
    public void testSimplePutAndGet() {
        postgresqlContainer.isRunning();
    }
}
