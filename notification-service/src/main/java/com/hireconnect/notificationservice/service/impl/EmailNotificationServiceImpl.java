package com.hireconnect.notificationservice.service.impl;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.hireconnect.notificationservice.service.EmailNotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationServiceImpl implements EmailNotificationService {

    private final JavaMailSender mailSender;

    @Override
    public void sendLoginNotification(String to, String userName, String loginTime) {
        String subject = "Login Alert - HireConnect";
        String body = buildLoginEmailBody(userName, loginTime);
        
        sendEmail(to, subject, body);
        log.info("Login notification sent to: {} for user: {}", to, userName);
    }

    @Override
    public void sendNotificationEmail(String to, String notificationTitle, String notificationMessage, String notificationType) {
        String subject = "New Notification - " + notificationTitle;
        String body = buildNotificationEmailBody(notificationTitle, notificationMessage, notificationType);
        
        sendEmail(to, subject, body);
        log.info("Notification email sent to: {} for type: {}", to, notificationType);
    }

    @Override
    public void sendJobApplicationNotification(String to, String jobTitle, String companyName) {
        String subject = "Job Application Received - " + jobTitle;
        String body = buildJobApplicationEmailBody(jobTitle, companyName);
        
        sendEmail(to, subject, body);
        log.info("Job application notification sent to: {} for job: {}", to, jobTitle);
    }

    @Override
    public void sendInterviewScheduleNotification(String to, String jobTitle, String companyName, String interviewDate) {
        String subject = "Interview Scheduled - " + jobTitle;
        String body = buildInterviewScheduleEmailBody(jobTitle, companyName, interviewDate);
        
        sendEmail(to, subject, body);
        log.info("Interview schedule notification sent to: {} for job: {}", to, jobTitle);
    }

    @Override
    public void sendApplicationStatusNotification(String to, String jobTitle, String status) {
        String subject = "Application Status Update - " + jobTitle;
        String body = buildApplicationStatusEmailBody(jobTitle, status);
        
        sendEmail(to, subject, body);
        log.info("Application status notification sent to: {} for job: {} with status: {}", to, jobTitle, status);
    }

    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        
        mailSender.send(message);
    }

    private String buildLoginEmailBody(String userName, String loginTime) {
        return """
            Hello %s,
            
            We noticed a new login to your HireConnect account.
            
            Login Details:
            - User: %s
            - Time: %s
            - Location: Web Browser
            
            If this was you, no action is needed.
            If you didn't recognize this login, please secure your account immediately.
            
            Best regards,
            The HireConnect Team
            """.formatted(userName, userName, loginTime);
    }

    private String buildNotificationEmailBody(String notificationTitle, String notificationMessage, String notificationType) {
        return """
            Hello,
            
            You have a new notification on HireConnect.
            
            Type: %s
            Title: %s
            Message: %s
            
            Please log in to your account to view more details.
            
            Best regards,
            The HireConnect Team
            """.formatted(notificationType, notificationTitle, notificationMessage);
    }

    private String buildJobApplicationEmailBody(String jobTitle, String companyName) {
        return """
            Hello,
            
            Great news! Someone has applied to your job posting.
            
            Job Details:
            - Position: %s
            - Company: %s
            - Applied: %s
            
            Please log in to your account to review the application.
            
            Best regards,
            The HireConnect Team
            """.formatted(jobTitle, companyName, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
    }

    private String buildInterviewScheduleEmailBody(String jobTitle, String companyName, String interviewDate) {
        return """
            Hello,
            
            Your interview has been scheduled!
            
            Interview Details:
            - Position: %s
            - Company: %s
            - Date: %s
            
            Please make sure to prepare and attend the interview on time.
            
            Best regards,
            The HireConnect Team
            """.formatted(jobTitle, companyName, interviewDate);
    }

    private String buildApplicationStatusEmailBody(String jobTitle, String status) {
        return """
            Hello,
            
            Your application status has been updated.
            
            Application Details:
            - Position: %s
            - Status: %s
            - Updated: %s
            
            Please log in to your account to view more details.
            
            Best regards,
            The HireConnect Team
            """.formatted(jobTitle, status, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
    }
}
