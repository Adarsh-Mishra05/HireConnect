package com.hireconnect.notificationservice.service;

public interface EmailNotificationService {
    
    void sendLoginNotification(String to, String userName, String loginTime);
    
    void sendNotificationEmail(String to, String notificationTitle, String notificationMessage, String notificationType);
    
    void sendJobApplicationNotification(String to, String jobTitle, String companyName);
    
    void sendInterviewScheduleNotification(String to, String jobTitle, String companyName, String interviewDate);
    
    void sendApplicationStatusNotification(String to, String jobTitle, String status);
}
