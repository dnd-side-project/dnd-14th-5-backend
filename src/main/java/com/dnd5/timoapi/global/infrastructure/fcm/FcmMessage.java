package com.dnd5.timoapi.global.infrastructure.fcm;

import java.util.Map;

public record FcmMessage(String title, String body, Map<String, String> data) {

    public static FcmMessage of(String title, String body) {
        return new FcmMessage(title, body, Map.of());
    }

    public static FcmMessage of(String title, String body, Map<String, String> data) {
        return new FcmMessage(title, body, data);
    }
}
