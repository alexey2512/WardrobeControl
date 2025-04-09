package com.myapp.database;

import org.testcontainers.containers.PostgreSQLContainer;

public class TestContainer extends PostgreSQLContainer<TestContainer> {

    private static final String IMAGE = "postgres:latest";
    private static final String USERNAME = "user";
    private static final String PASSWORD = "password";
    private static final String DATABASE = "wardrobe_control_db";
    private static final String SCRIPT = "init.sql";

    public static final String DRIVER = "org.postgresql.Driver";

    public TestContainer() {
        super(IMAGE);
    }

    public TestContainer make() {
        return this
                .withUsername(USERNAME)
                .withPassword(PASSWORD)
                .withDatabaseName(DATABASE)
                .withInitScript(SCRIPT);
    }

}
