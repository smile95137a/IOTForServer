package com.frontend.config.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.frontend.config.service.UserPrinciple;

public class SecurityUtils {

	public static final String ROLE_DEFAULT = "ROLE_XXX";

	public static Authentication getAuthenticationObject() {
		return SecurityContextHolder.getContext().getAuthentication();
	}

	public static UserPrinciple getSecurityUser() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof UserPrinciple) {
			return (UserPrinciple) principal;
		} else {
			throw new RuntimeException("獲取當前用戶失敗，Principal 類型錯誤：" + principal.getClass().getName());
		}
	}


}
