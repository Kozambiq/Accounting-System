package com.raven.main;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory user-specific notifications for Trial Balance and Financial Reports.
 * Deduplication: same key + same message within a short window is not duplicated.
 */
public final class NotificationHolder {

    /** No limit: display all notifications until manually cleared. */
    private static final int MAX_PER_USER = 100_000;
    private static final long DEDUP_MS = 2_000;

    /** userId -> list of (message, createdMs) */
    private static final Map<Integer, LinkedList<NotificationEntry>> BY_USER = new ConcurrentHashMap<>();
    /** userId -> (key -> last message + time) for deduplication */
    private static final Map<Integer, Map<String, DedupEntry>> LAST_BY_KEY = new ConcurrentHashMap<>();

    private NotificationHolder() {}

    public static void add(Integer userId, String key, String message) {
        if (userId == null || message == null || message.isBlank()) return;
        Map<String, DedupEntry> keyMap = LAST_BY_KEY.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
        long now = System.currentTimeMillis();
        DedupEntry last = keyMap.get(key);
        if (last != null && last.message.equals(message) && (now - last.timeMs) < DEDUP_MS) {
            return;
        }
        keyMap.put(key, new DedupEntry(message, now));
        LinkedList<NotificationEntry> list = BY_USER.computeIfAbsent(userId, k -> new LinkedList<>());
        list.addFirst(new NotificationEntry(message, now));
        while (list.size() > MAX_PER_USER) list.removeLast();
    }

    public static List<String> getRecent(Integer userId, int limit) {
        if (userId == null) return List.of();
        LinkedList<NotificationEntry> list = BY_USER.get(userId);
        if (list == null) return List.of();
        List<String> out = new ArrayList<>(Math.min(limit, list.size()));
        int n = 0;
        for (NotificationEntry e : list) {
            if (n >= limit) break;
            out.add(e.message);
            n++;
        }
        return out;
    }

    /** Clear all notifications for the given user (manual clear). */
    public static void clearAll(Integer userId) {
        if (userId == null) return;
        BY_USER.remove(userId);
        LAST_BY_KEY.remove(userId);
    }

    private static class NotificationEntry {
        final String message;
        final long timeMs;
        NotificationEntry(String message, long timeMs) { this.message = message; this.timeMs = timeMs; }
    }

    private static class DedupEntry {
        final String message;
        final long timeMs;
        DedupEntry(String message, long timeMs) { this.message = message; this.timeMs = timeMs; }
    }
}
