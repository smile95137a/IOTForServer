package src.main.java.backend.utils;

import java.util.Collection;

import src.main.java.backend.config.service.UserPrinciple;
import src.main.java.backend.constant.AuthoritiesConstants;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;


public class SecurityUtils {

	public static UserPrinciple getSecurityUser() {
		return (UserPrinciple) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	public static String getUsername() {
		SecurityContext securityContext = SecurityContextHolder.getContext();
		Authentication authentication = securityContext.getAuthentication();
		UserDetails springSecurityUser = null;
		String userName = null;

		if (authentication != null) {
			if (authentication.getPrincipal() instanceof UserDetails) {
				springSecurityUser = (UserDetails) authentication.getPrincipal();
				userName = springSecurityUser.getUsername();
			} else if (authentication.getPrincipal() instanceof String) {
				userName = (String) authentication.getPrincipal();
			}
		}

		return userName;
	}

	public static boolean isAuthenticated() {
		SecurityContext securityContext = SecurityContextHolder.getContext();

		final Collection<? extends GrantedAuthority> authorities = securityContext.getAuthentication().getAuthorities();

		if (authorities != null) {
			for (GrantedAuthority authority : authorities) {
				if (AuthoritiesConstants.ANONYMOUS.equals(authority.getAuthority())) {
					return false;
				}
			}
		}

		return true;
	}

}
