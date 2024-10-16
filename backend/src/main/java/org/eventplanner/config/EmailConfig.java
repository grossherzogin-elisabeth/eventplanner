package org.eventplanner.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class EmailConfig {

    private final String host;
    private final Integer port;
    private final String protocol;
    private final String username;
    private final String password;
    private final Boolean starttls;

    public EmailConfig(
        @Value("${email.host}") String host,
        @Value("${email.port}") Integer port,
        @Value("${email.protocol}") String protocol,
        @Value("${email.username}") String username,
        @Value("${email.password}") String password,
        @Value("${email.starttls}") Boolean starttls
    ) {
        this.host = host;
        this.port = port;
        this.protocol = protocol;
        this.username = username;
        this.password = password;
        this.starttls = starttls;
    }

    @Bean
    public JavaMailSender javaMailSender() {
        final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setProtocol(protocol);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        final Properties javaMailProperties = new Properties();
        javaMailProperties.put("mail.smtp.auth", true);
        javaMailProperties.put("mail.smtp.starttls.enable", starttls);
        mailSender.setJavaMailProperties(javaMailProperties);
        return mailSender;
    }
}
