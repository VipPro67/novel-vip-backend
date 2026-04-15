package com.novel.vippro.Messaging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.novel.vippro.DTO.Comment.CommentDTO;
import com.novel.vippro.DTO.Notification.NotificationDTO;

import com.novel.vippro.Controllers.NotificationStreamController;

@Component
@ConditionalOnProperty(name = "app.messaging.provider", havingValue = "activemq")
public class ActiveMQNovelVipListener {

    private final NotificationStreamController notificationStreamController;

    public ActiveMQNovelVipListener(NotificationStreamController notificationStreamController) {
        this.notificationStreamController = notificationStreamController;
    }

    @JmsListener(destination = MessageQueues.NOTIFICATION)
    public void handleNotification(NotificationDTO notification) {
        notificationStreamController.sendNotificationToUser(notification.userId(), notification);
    }

    @JmsListener(destination = MessageQueues.COMMENT)
    public void handleComment(CommentDTO comment) {
        // SSE is standard, no WS push needed
    }
}
