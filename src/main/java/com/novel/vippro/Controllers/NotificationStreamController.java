package com.novel.vippro.Controllers;

import com.novel.vippro.Security.UserDetailsImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
// IMPORTANT: SSE uses cookies (EventSource withCredentials) in this app.
// Using "*" breaks credentialed CORS in browsers; keep this aligned with WebSecurityConfig.
@CrossOrigin(
        origins = { "https://novel-vip.vercel.app", "http://localhost:3000" },
        allowCredentials = "true")
@RequestMapping("/api/notifications")
@Log4j2
public class NotificationStreamController {

    private static final long SSE_TIMEOUT = 5 * 60 * 1000L;
    private static final long HEARTBEAT_INTERVAL = 15 * 1000L;

    // userId -> (deviceId -> SseEmitter)
    private final Map<UUID, Map<String, SseEmitter>> userEmitters = new ConcurrentHashMap<>();
    private final ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();

    public NotificationStreamController() {
        heartbeatScheduler.scheduleAtFixedRate(
                this::sendHeartbeats,
                HEARTBEAT_INTERVAL,
                HEARTBEAT_INTERVAL,
                TimeUnit.MILLISECONDS);
    }

    private void sendHeartbeats() {
        for (Map.Entry<UUID, Map<String, SseEmitter>> userEntry : userEmitters.entrySet()) {
            Map<String, SseEmitter> deviceMap = userEntry.getValue();
            for (Map.Entry<String, SseEmitter> deviceEntry : deviceMap.entrySet()) {
                try {
                    // Some proxies ignore comment-only frames; send a lightweight named event.
                    deviceEntry.getValue().send(SseEmitter.event().name("heartbeat").data(""));
                } catch (Exception e) {
                    deviceMap.remove(deviceEntry.getKey(), deviceEntry.getValue());
                }
            }
            if (deviceMap.isEmpty()) {
                userEmitters.computeIfPresent(userEntry.getKey(), (k, v) -> v.isEmpty() ? null : v);
            }
        }
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("isAuthenticated()")
    public SseEmitter streamNotifications(
            @RequestParam(value = "deviceId", defaultValue = "default") String deviceId,
            HttpServletResponse response) {
        UUID userId = UserDetailsImpl.getCurrentUserId();

        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-transform");
        response.setHeader(HttpHeaders.CONNECTION, "keep-alive");
        response.setHeader("X-Accel-Buffering", "no");

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        userEmitters.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
        
        Map<String, SseEmitter> deviceMap = userEmitters.get(userId);
        SseEmitter oldEmitter = deviceMap.put(deviceId, emitter);
        
        if (oldEmitter != null) {
            oldEmitter.complete();
        }

        Runnable cleanup = () -> {
            Map<String, SseEmitter> map = userEmitters.get(userId);
            if (map != null) {
                map.remove(deviceId, emitter);
                userEmitters.computeIfPresent(userId, (k, v) -> v.isEmpty() ? null : v);
            }
        };

        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        try {
            emitter.send(SseEmitter.event().name("connected").data("Connection established"));
        } catch (IOException e) {
            cleanup.run();
        }

        return emitter;
    }

    public void sendNotificationToUser(UUID userId, Object notification) {
        Map<String, SseEmitter> deviceMap = userEmitters.get(userId);
        if (deviceMap == null || deviceMap.isEmpty())
            return;

        for (Map.Entry<String, SseEmitter> entry : deviceMap.entrySet()) {
            SseEmitter emitter = entry.getValue();
            try {
                emitter.send(SseEmitter.event().name("notification").data(notification));
                log.info("SSE notification sent to userId={} deviceId={}", userId, entry.getKey());
            } catch (Exception e) {
                log.warn("SSE send failed; removing emitter for userId={} deviceId={}", userId, entry.getKey(), e);
                deviceMap.remove(entry.getKey(), emitter);
            }
        }
    }

    public int getActiveConnectionCount() {
        return userEmitters.values().stream().mapToInt(Map::size).sum();
    }

    @GetMapping("/connections/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getConnectionStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalConnections", getActiveConnectionCount());
        stats.put("uniqueUsers", userEmitters.size());
        return ResponseEntity.ok(stats);
    }
}
