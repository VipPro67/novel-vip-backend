package com.novel.vippro.Messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.novel.vippro.Controllers.NotificationStreamController;
import com.novel.vippro.DTO.Comment.CommentDTO;
import com.novel.vippro.DTO.Notification.NotificationDTO;
import com.novel.vippro.Messaging.payload.ChapterAudioMessage;
import com.novel.vippro.Messaging.payload.EmailVerificationMessage;
import com.novel.vippro.Messaging.payload.EpubImportMessage;
import com.novel.vippro.Messaging.payload.ShubaImportMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@ConditionalOnProperty(name = "app.messaging.provider", havingValue = "rabbitmq", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class RabbitMessageListener {

    private final EpubImportProcessor epubProcessor;
    private final ShubaImportProcessor shubaProcessor;
    private final ChapterAudioProcessor audioProcessor;
    private final EmailVerificationJobHandler emailVerificationHandler;
	private final NotificationStreamController notificationStreamController;

    @RabbitListener(queues = MessageQueues.NOTIFICATION)
    public void handleNotification(NotificationDTO notification) {
        log.debug("Received notification for user {}", notification.userId());
		notificationStreamController.sendNotificationToUser(notification.userId(), notification);
    }

    @RabbitListener(queues = MessageQueues.COMMENT)
    public void handleComment(CommentDTO comment) {
        log.debug("Received comment {}, skipping WS push since SSE is standard", comment.id());
    }

    @RabbitListener(queues = MessageQueues.EPUB_UPLOAD)
    public void handleEpubUpload(EpubImportMessage message) {
        log.info("Received EPUB import job {}", message.getJobId());
        epubProcessor.process(message);
    }

    @RabbitListener(queues = MessageQueues.SHUBA_IMPORT)
    public void handleShubaImport(ShubaImportMessage message) {
        log.info("Received Shuba import job {}", message.getJobId());
        shubaProcessor.process(message);
    }

    @RabbitListener(queues = MessageQueues.CHAPTER_AUDIO)
    public void handleChapterAudio(ChapterAudioMessage message) {
        log.info("Received chapter audio job for chapter {}", message.getChapterId());
        audioProcessor.process(message);
    }

    @RabbitListener(queues = MessageQueues.EMAIL_VERIFICATION)
    public void handleEmailVerification(EmailVerificationMessage message) {
        log.info("Received email verification job for user {}", message.getUserId());
        emailVerificationHandler.handle(message);
    }
}
