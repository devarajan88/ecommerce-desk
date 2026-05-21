package com.appsdeveloperblog.users.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine){
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    };

    public void sendEmail(String to,
                          String subject,
                          String userName,
                          String messageBody,
                          String hrName) throws MessagingException {

        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("message", messageBody);
        context.setVariable("hrName", hrName);

        String htmlContent =
                templateEngine.process("user-communication", context);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper =
                new MimeMessageHelper(mimeMessage, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // TRUE = HTML

        mailSender.send(mimeMessage);
    }
}
