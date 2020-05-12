package com.ceer.niukeblog.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * @ClassName MailClient
 * @Description 发送邮件
 * @Author ceer
 * @Date 2020/4/29 22:23
 * @Version 1.0
 */
@Slf4j
@Component
public class MailClient {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String username;

    public void sendMail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            //发件人
            helper.setFrom(username);
            //收件人
            helper.setTo(to);
            //设置主题
            helper.setSubject(subject);
            //设置文本内容
            helper.setText(content, true);
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            log.error("发送邮件失败:" + e.getMessage());
        }
    }

}
