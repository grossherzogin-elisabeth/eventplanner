package org.eventplanner.notifications.service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.eventplanner.notifications.entities.Notification;
import org.eventplanner.settings.adapter.SettingsRepository;
import org.eventplanner.settings.service.SettingsService;
import org.eventplanner.users.entities.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

@Service
public class NotificationService {

    private final Configuration freemarkerConfig;
    private final SettingsService settingsService;

    @Autowired
    public NotificationService(
            Configuration freemarkerConfig,
            SettingsService settingsService
    ) {
        this.freemarkerConfig = freemarkerConfig;
        this.settingsService = settingsService;
    }

    private JavaMailSender getMailSender() {
        var settings = settingsService.getSettings().emailSettings();

        final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(settings.getHost());
        mailSender.setPort(settings.getPort());
        mailSender.setProtocol("smtp");
        mailSender.setUsername(settings.getUsername());
        mailSender.setPassword(settings.getPassword());

        final Properties javaMailProperties = new Properties();
        javaMailProperties.put("mail.smtp.auth", true);
        javaMailProperties.put("mail.smtp.starttls.enable", settings.getEnableStartTls());
        javaMailProperties.put("mail.smtp.ssl.enable", settings.getEnableSSL());
        mailSender.setJavaMailProperties(javaMailProperties);
        return mailSender;
    }

    public void sendNotification(Notification notification, List<UserDetails> toUsers) throws IOException, TemplateException, MessagingException {
        var mailSender = getMailSender();
        MimeMessage message = mailSender.createMimeMessage();
        message.setFrom(new InternetAddress("noreplay@grossherzogin-elisabeth.de"));
        message.setRecipients(MimeMessage.RecipientType.TO, "malte.schwitters@outlook.de");
        message.setSubject("Test email from Spring");

        var htmlContent = renderEmail(notification, toUsers.getFirst());
        message.setContent(htmlContent, "text/html; charset=utf-8");

        mailSender.send(message);
    }

    private String renderEmail(Notification notification, UserDetails user) throws IOException, TemplateException {
        var props = notification.getProps();
        props.put("title", notification.getTitle());
        props.put("user.name", user.getFirstName());
        String content = renderTemplate("emails/" + notification.getType().toString() + ".ftl", props);

        var baseTemplateParams = new HashMap<String, Object>();
        baseTemplateParams.put("title", notification.getTitle());
        baseTemplateParams.put("content", content);
        baseTemplateParams.put("footer", "bups");
        return renderTemplate("partials/base.ftl", baseTemplateParams);
    }

    private String renderTemplate(String template, Object params) throws TemplateException, IOException {
        Template content = freemarkerConfig.getTemplate(template);
        Writer writer = new StringWriter();
        content.process(params, writer);
        return writer.toString();
    }
}
