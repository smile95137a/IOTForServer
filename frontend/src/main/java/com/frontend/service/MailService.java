package com.frontend.service;

import java.time.LocalDate;
import java.util.HashMap;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

	@Autowired
	private JavaMailSender emailSender;

	@Autowired
	private SpringTemplateEngine templateEngine;

	@Value("${custom.mail.from}")
	private String from;

	public void sendVerificationMail(String to, String verificationUrl) {
		// 创建 MIME 邮件
		MimeMessage mimeMessage = emailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

		try {
			helper.setFrom(from);
			helper.setTo(to);
			helper.setSubject("升級再來一抽認證會員");
			// 将邮件内容设置为 HTML 格式
			String htmlContent = "<p>請點擊網址升級成認證會員感謝您:</p>" +
					"<a href=\"" + verificationUrl + "\">" + verificationUrl + "</a>";
			helper.setText(htmlContent, true); // 第二个参数设为 true 表示内容为 HTML

			emailSender.send(mimeMessage);
		} catch (MessagingException e) {
			e.printStackTrace(); // 处理邮件发送异常
		}
	}

	public void simpleSend(String email, String subject, String emailMessage) {

		try {
			var message = emailSender.createMimeMessage();
			var helper = new MimeMessageHelper(message);

			helper.setTo(email);
			helper.setSubject(subject);
			helper.setText(emailMessage, true);

			emailSender.send(message);

		} catch (Exception e) {
			log.error("Failed to send email", e);
		}

	}

	public void sendPasswordEmail(String email, String name, String password) {
		try {
			var message = emailSender.createMimeMessage();
			var helper = new MimeMessageHelper(message);

			helper.setTo(email);
			helper.setSubject("密碼確認信");

			var properties = new HashMap<String, Object>();
			properties.put("name", name);
			properties.put("password", password);
			properties.put("year", LocalDate.now().getYear());

			var context = new Context();
			context.setVariables(properties);

			var html = templateEngine.process("emails/emailPassword.html", context);
			helper.setText(html, true);

			emailSender.send(message);

		} catch (Exception e) {
			log.error("Failed to send email", e);
		}
	}
}
