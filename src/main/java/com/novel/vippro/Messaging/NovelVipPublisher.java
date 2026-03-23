package com.novel.vippro.Messaging;

import com.novel.vippro.Controllers.NotificationStreamController;
import com.novel.vippro.DTO.Comment.CommentDTO;
import com.novel.vippro.DTO.Notification.NotificationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class NovelVipPublisher {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationStreamController notificationStreamController;

    public void publishNotification(NotificationDTO notification) {
        // Send via SSE
        notificationStreamController.sendNotificationToUser(notification.userId(), notification);

        // Keep WebSocket for backward compatibility
        messagingTemplate.convertAndSend(
                "/topic/user." + notification.userId(),
                notification);
    }

    /**
     * Pushes a new comment to the chapter topic so live subscribers see it
     * immediately. Destination aligns with the frontend subscription at
     * {@code /topic/chapter.<chapterId>}.
     */
    public void publishComment(CommentDTO comment) {
        // Primary: chapter-scoped topic (matches frontend subscriber)
        if (comment.chapterId() != null) {
            messagingTemplate.convertAndSend(
                    "/topic/chapter." + comment.chapterId(),
                    comment);
        }

        // Secondary: novel-level topic for any novel-wide listeners
        StringBuilder novelDest = new StringBuilder("/topic/novel.").append(comment.novelId());
        if (comment.chapterId() != null) {
            novelDest.append(".chapter.").append(comment.chapterId());
        }
        messagingTemplate.convertAndSend(novelDest.toString(), comment);
    }

    /**
     * Notifies the frontend that audio generation for a chapter is complete.
     * Destination matches the frontend subscription at
     * {@code /topic/chapter.<chapterId>.audio}.
     */
    public void publishAudioReady(String chapterId, String audioUrl) {
        messagingTemplate.convertAndSend(
                "/topic/chapter." + chapterId + ".audio",
                Map.of("audioUrl", audioUrl, "chapterId", chapterId));
    }
}
