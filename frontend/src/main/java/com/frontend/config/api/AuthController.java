package com.frontend.config.api;

import java.util.stream.Collectors;

import com.frontend.config.jwt.JwtProvider;
import com.frontend.config.service.UserPrinciple;
import com.frontend.enums.RoleName;
import com.frontend.req.user.UserReq;
import com.frontend.res.user.UserRes;
import com.frontend.service.UserService;
import com.frontend.utils.ResponseUtils;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final UserService userService;
	private final JwtProvider jwtProvider;

	@PostMapping("/login")
	public ResponseEntity<?> login(@Valid @RequestBody UserReq userReq) {
		var authToken = new UsernamePasswordAuthenticationToken(userReq.getEmail(), userReq.getPassword());
		var authentication = authenticationManager.authenticate(authToken);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		var userDetails = (UserPrinciple) authentication.getPrincipal();
		
		var jwt = jwtProvider.generateToken(userDetails);
		
		var roles = userDetails.getAuthorities().stream().map(x -> RoleName.valueOf(x.getAuthority()).toString())
				.collect(Collectors.toSet());
		
		var userRes = UserRes.builder()
				.uid(userDetails.getUid())
				.email(userDetails.getEmail())
				.name(userDetails.getName())
				.roles(roles)
				.build();
		
		var result = JwtResponse.builder()
				.accessToken(jwt)
				.user(userRes)
				.build();
		
		var res = ResponseUtils.success(result);
		return ResponseEntity.ok(res);
	}

}