package com.dnd5.timoapi.global.infrastructure.fcm;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmSender {

    private final FcmProperties properties;

    @Async
    public void sendToToken(String token, FcmMessage fcmMessage) {
        if (!properties.enabled()) return;

        try {
            Message message = buildMessage(token, fcmMessage);
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            log.error("FCM send failed: {}", e.getMessage());
        }
    }

    @Async
    public void sendToTokens(List<String> tokens, FcmMessage fcmMessage) {
        if (!properties.enabled() || tokens.isEmpty()) return;

        try {
            MulticastMessage message = buildMulticastMessage(tokens, fcmMessage);
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            if (response.getFailureCount() > 0) {
                log.warn("FCM multicast partial failure: success={}, failure={}",
                        response.getSuccessCount(), response.getFailureCount());
            }
        } catch (FirebaseMessagingException e) {
            log.error("FCM multicast failed: {}", e.getMessage());
        }
    }

    private Message buildMessage(String token, FcmMessage fcmMessage) {
        Message.Builder builder = Message.builder()
                .setToken(token)
                .setNotification(buildNotification(fcmMessage));

        if (fcmMessage.data() != null && !fcmMessage.data().isEmpty()) {
            builder.putAllData(fcmMessage.data());
        }

        return builder.build();
    }

    private MulticastMessage buildMulticastMessage(List<String> tokens, FcmMessage fcmMessage) {
        MulticastMessage.Builder builder = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(buildNotification(fcmMessage));

        if (fcmMessage.data() != null && !fcmMessage.data().isEmpty()) {
            builder.putAllData(fcmMessage.data());
        }

        return builder.build();
    }

    private Notification buildNotification(FcmMessage fcmMessage) {
        return Notification.builder()
                .setTitle(fcmMessage.title())
                .setBody(fcmMessage.body())
                .build();
    }
}
