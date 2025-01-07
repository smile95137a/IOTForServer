package backend.service;

import java.time.LocalDate;
import java.util.HashMap;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

	private final JavaMailSender emailSender;

	private final SpringTemplateEngine templateEngine;

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
