package org.eventplanner.events.application.services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.eventplanner.events.domain.entities.notifications.GlobalNotification;
import org.eventplanner.events.domain.entities.notifications.Notification;
import org.eventplanner.events.domain.values.notifications.NotificationType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamsNotificationService implements NotificationDispatcher {

    private final ConfigurationService configurationService;

    @Override
    public void dispatch(@NonNull final Notification notification) {
        // TODO maybe also specify channel by role?
        if (notification instanceof GlobalNotification) {
            var teamsUrl = configurationService.getConfig().notifications().teamsWebhookUrl();
            if (teamsUrl == null) {
                log.debug(
                    "Skipping teams notification for {}, because no teams webhook url is configured",
                    notification.type()
                );
                return;
            }
            try {
                var uri = URI.create(teamsUrl);
                switch (notification.type()) {
                    case NotificationType.USER_DATA_CHANGED -> createTeamsAlert(uri, notification);
                    case NotificationType.CREW_REGISTRATION_CANCELED -> createTeamsAlert(uri, notification);
                    case NotificationType.CREW_REGISTRATION_ADDED -> createTeamsAlert(uri, notification);
                }
            } catch (Exception e) {
                log.error("Failed to create MS Teams alert for {} notification", notification.type(), e);
            }
        }
    }

    public void createTeamsAlert(@NonNull final URI webhookUri, @NonNull final Notification notification)
    throws IOException, InterruptedException {
        log.info("Creating MS Teams alert for {} notification", notification.type());
        var body = renderDefaultTeamsMessage(notification);
        var request = HttpRequest.newBuilder()
            .method("POST", HttpRequest.BodyPublishers.ofString(body))
            .uri(webhookUri)
            .build();
        try (var client = HttpClient.newBuilder().build()) {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.error(
                    "Failed to create MS Teams alert for {} notification. Webhook failed with status code {}",
                    notification.type(),
                    response.statusCode()
                );
            }
        }
    }

    private @NonNull String renderDefaultTeamsMessage(@NonNull final Notification notification) {
        return """
            {
                "type": "message",
                "attachments": [
            
            
                    {
                        "contentType": "application/vnd.microsoft.card.adaptive",
                        "contentUrl": null,
                        "content": {
                            "$schema": "http://adaptivecards.io/schemas/adaptive-card.json",
                            "type": "AdaptiveCard",
                            "version": "1.2",
                            "body": [
                                {
                                    "type": "TextBlock",
                                    "wrap": true,
                                    "text": "%s"
                                }
                            ],
                            "actions": [
                                {
                                    "type": "Action.OpenUrl",
                                    "title": "In Crew App anzeigen",
                                    "tooltip": "In der Crew App anzeigen",
                                    "url": "%s"
                                }
                            ]
                        }
                    }
                ]
            }
            """.formatted(
            notification.summary(),
            notification.link()
        );
    }
}
