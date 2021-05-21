package org.nature.websocket.server;

import org.apache.commons.lang3.StringUtils;
import org.nature.websocket.model.UserInfo;

import javax.websocket.Session;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SessionContext {

    private static final Map<String, List<Session>> systemMap = new ConcurrentHashMap<>();

    private static final Map<String, List<Session>> userMap = new ConcurrentHashMap<>();

    private static final Map<UserInfo, List<Session>> userInfoMap = new ConcurrentHashMap<>();

    private static final Map<Session, UserInfo> sessionMap = new ConcurrentHashMap<>();

    private static final Map<UserInfo, Object> userInfoLockMap = new ConcurrentHashMap<>();

    private static final Map<String, Object> systemLockMap = new ConcurrentHashMap<>();

    private static final Map<String, Object> userLockMap = new ConcurrentHashMap<>();

    public static void addSession(UserInfo u, Session s) {
        if (u == null || (StringUtils.isBlank(u.getSystem()) && StringUtils.isBlank(u.getId()))) {
            return;
        }
        processAdd(u, s, userInfoLockMap, userInfoMap);
        String system = u.getSystem();
        if (StringUtils.isNotBlank(system)) {
            processAdd(system, s, systemLockMap, systemMap);
        }
        String id = u.getId();
        if (StringUtils.isNotBlank(id)) {
            processAdd(id, s, userLockMap, userMap);
        }
        sessionMap.put(s, u);
    }

    public static void removeSession(Session s) {
        UserInfo u = sessionMap.get(s);
        if (u == null) {
            return;
        }
        processRemove(u, s, userInfoLockMap, userInfoMap);
        String system = u.getSystem();
        if (StringUtils.isNotBlank(system)) {
            processRemove(system, s, systemLockMap, systemMap);
        }
        String id = u.getId();
        if (StringUtils.isNotBlank(id)) {
            processRemove(id, s, userLockMap, userMap);
        }
        sessionMap.remove(s);
    }

    public static List<Session> getSessions(UserInfo u) {
        Set<Session> sessions = new HashSet<>();
        if (u == null) {
            return new ArrayList<>(sessions);
        }
        List<Session> us = userInfoMap.get(u);
        if (us != null) {
            sessions.addAll(us);
        }
        String system = u.getSystem();
        String id = u.getId();
        if (StringUtils.isNotBlank(system) && StringUtils.isBlank(id)) {
            List<Session> ss = systemMap.get(system);
            if (ss != null) {
                sessions.addAll(ss);
            }
        }
        if (StringUtils.isNotBlank(id) && StringUtils.isBlank(system)) {
            List<Session> ss = userMap.get(id);
            if (ss != null) {
                sessions.addAll(ss);
            }
        }
        return new ArrayList<>(sessions);
    }

    private static <T> void processAdd(T t, Session s, Map<T, Object> lockMap, Map<T, List<Session>> map) {
        Object o = lockMap.computeIfAbsent(t, k -> new Object());
        synchronized (o) {
            List<Session> list = map.computeIfAbsent(t, k -> new ArrayList<>());
            list.add(s);
            lockMap.remove(t);
        }
    }

    private static <T> void processRemove(T t, Session s, Map<T, Object> lockMap, Map<T, List<Session>> map) {
        Object o = lockMap.computeIfAbsent(t, k -> new Object());
        synchronized (o) {
            List<Session> list = map.get(t);
            if (list != null) {
                list.remove(s);
                if (list.isEmpty()) {
                    map.remove(t);
                }
            }
            lockMap.remove(t);
        }
    }

    public static void print() {
        System.out.println("systemMap:" + systemMap);
        System.out.println("userMap:" + userMap);
        System.out.println("userInfoMap:" + userInfoMap);
        System.out.println("sessionMap:" + sessionMap);
    }

}
