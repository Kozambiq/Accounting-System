package com.raven.main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Central place to obtain a connection to the SQLite user database.
 *
 * The database file lives under {@code src/main/resources/database/users.db}.
 * This method returns a new connection each time it is called; the caller is
 * responsible for closing it (for example by using try-with-resources).
 */
public class DBConnection {

    // Path is relative to the working directory when running the app.
    // Adjust if you move the database file.
    private static final String URL = "jdbc:sqlite:src/main/resources/database/app.db";

    /**
     * Open a new connection to the SQLite database.
     *
     * @return an open {@link Connection}
     * @throws SQLException if the connection cannot be established
     */
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}

