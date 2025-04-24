package com.frontend.config.jwt;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.frontend.config.message.ApiResponse;
import com.frontend.utils.ResponseUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request,
						 HttpServletResponse response,
						 AuthenticationException authException) throws IOException {

		log.error("Unauthorized error. Message - {}", authException.getMessage());

		response.setContentType("application/json;charset=UTF-8");
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

		ApiResponse<String> error = ResponseUtils.error(9999 ,"帳號或密碼錯誤" , null);
		response.getWriter().write(new ObjectMapper().writeValueAsString(error));
	}


}
