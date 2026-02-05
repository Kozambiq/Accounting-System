package com.raven.main;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates the core SQLite schema on application startup if it does not exist
 * yet. This is where we enforce that every data table is tied to a user
 * through a {@code user_id} foreign key.
 */
public final class DatabaseInitializer {

    private DatabaseInitializer() {
    }

    public static void initialize() {
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement()) {

            // Always enable foreign keys in SQLite.
            stmt.execute("PRAGMA foreign_keys = ON");

            // --- USERS ------------------------------------------------------
            // Keep this aligned with the existing signUpPage SQL, which inserts
            // into (first_name, last_name, email, password).
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        id          INTEGER PRIMARY KEY AUTOINCREMENT,
                        first_name  TEXT    NOT NULL,
                        last_name   TEXT    NOT NULL,
                        email       TEXT    NOT NULL UNIQUE,
                        password    TEXT    NOT NULL
                    );
                    """);

            // --- ASSETS -----------------------------------------------------
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS assets (
                        id          INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id     INTEGER NOT NULL,
                        name        TEXT    NOT NULL,
                        amount      REAL    NOT NULL,
                        created_at  TEXT    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                    );
                    """);

            // --- LIABILITIES -----------------------------------------------
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS liabilities (
                        id          INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id     INTEGER NOT NULL,
                        name        TEXT    NOT NULL,
                        amount      REAL    NOT NULL,
                        created_at  TEXT    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                    );
                    """);

            // --- EQUITY ----------------------------------------------------
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS equity (
                        id          INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id     INTEGER NOT NULL,
                        name        TEXT    NOT NULL,
                        amount      REAL    NOT NULL,
                        created_at  TEXT    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                    );
                    """);

            // --- JOURNAL ENTRIES ------------------------------------------
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS journal_entries (
                        id              INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id         INTEGER NOT NULL,
                        entry_date      TEXT    NOT NULL,
                        description     TEXT,
                        debit_account   TEXT    NOT NULL,
                        credit_account  TEXT    NOT NULL,
                        amount          REAL    NOT NULL,
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                    );
                    """);

            // --- REPORTS ---------------------------------------------------
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS reports (
                        id          INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id     INTEGER NOT NULL,
                        type        TEXT    NOT NULL,
                        period_from TEXT    NOT NULL,
                        period_to   TEXT    NOT NULL,
                        created_at  TEXT    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                    );
                    """);

        } catch (SQLException e) {
            // For a desktop app, logging to stderr is acceptable.
            e.printStackTrace();
        }
    }
}

