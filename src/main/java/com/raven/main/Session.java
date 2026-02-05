package com.raven.main;

/**
 * Simple in-memory session that stores information about the currently
 * authenticated user while the application is running.
 *
 * This is NOT persisted to disk. When the application exits the JVM, the
 * session is gone. Use it only for runtime access control (filtering queries
 * by the logged-in user's id, showing the user's name in the UI, etc.).
 */
public final class Session {

    private static Integer currentUserId;
    private static String currentUserName;
    private static String currentUserEmail;

    private Session() {
        // utility class
    }

    /**
     * Start a new session for the given user.
     *
     * @param userId   database primary key of the user (users.id)
     * @param fullName display name for the user
     * @param email    email address (login identifier)
     */
    public static void start(int userId, String fullName, String email) {
        currentUserId = userId;
        currentUserName = fullName;
        currentUserEmail = email;
    }

    /**
     * @return the id of the currently logged-in user, or {@code null} if
     *         no user is logged in.
     */
    public static Integer getCurrentUserId() {
        return currentUserId;
    }

    /**
     * @return the display name of the currently logged-in user,
     *         or {@code null} if none.
     */
    public static String getCurrentUserName() {
        return currentUserName;
    }

    /**
     * @return the email of the currently logged-in user,
     *         or {@code null} if none.
     */
    public static String getCurrentUserEmail() {
        return currentUserEmail;
    }

    /**
     * Clear the current session (for example on logout).
     */
    public static void clear() {
        currentUserId = null;
        currentUserName = null;
        currentUserEmail = null;
    }

    /**
     * @return {@code true} if there is an active logged-in user.
     */
    public static boolean isLoggedIn() {
        return currentUserId != null;
    }
}

