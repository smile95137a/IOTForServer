package com.frontend.config;//package backend.config;
//
//import java.nio.charset.StandardCharsets;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class MailConfig {
//
//	private static final String TEMPLATE_PREFIX = "/templates/";
//	private static final String TEMPLATE_SUFFIX = ".html";
//	private static final String TEMPLATE_MODE = "HTML";
//	private static final String CHARACTER_ENCODING = StandardCharsets.UTF_8.name();
//
//	@Bean
//	public SpringTemplateEngine springTemplateEngine() {
//		SpringTemplateEngine springTemplateEngine = new SpringTemplateEngine();
//		springTemplateEngine.addTemplateResolver(emailTemplateResolver());
//		return springTemplateEngine;
//	}
//
//	private ClassLoaderTemplateResolver emailTemplateResolver() {
//		ClassLoaderTemplateResolver emailTemplateResolver = new ClassLoaderTemplateResolver();
//		emailTemplateResolver.setPrefix(TEMPLATE_PREFIX);
//		emailTemplateResolver.setSuffix(TEMPLATE_SUFFIX);
//		emailTemplateResolver.setTemplateMode(TemplateMode.valueOf(TEMPLATE_MODE));
//		emailTemplateResolver.setCharacterEncoding(CHARACTER_ENCODING);
//		emailTemplateResolver.setCacheable(false);
//		return emailTemplateResolver;
//	}
//}
