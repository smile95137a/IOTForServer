package backend.config.api;

import java.util.stream.Collectors;

import backend.config.jwt.JwtProvider;
import backend.config.service.UserPrinciple;
import backend.enums.RoleName;
import backend.req.user.UserReq;
import backend.res.user.UserRes;
import backend.service.UserService;
import backend.utils.ResponseUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final UserService userService;
	private final JwtProvider jwtProvider;

	@PostMapping("/login")
	public ResponseEntity<?> login(@Valid @RequestBody UserReq userReq) throws Exception {
		var authToken = new UsernamePasswordAuthenticationToken(userReq.getUsername(), userReq.getPassword());
		var authentication = authenticationManager.authenticate(authToken);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		var userDetails = (UserPrinciple) authentication.getPrincipal();
		
		var jwt = jwtProvider.generateToken(userDetails);
		
		var roles = userDetails.getAuthorities().stream().map(x -> RoleName.valueOf(x.getAuthority()).toString())
				.collect(Collectors.toSet());

		if(roles.contains("ROLE_BLACKLIST")){
			throw new Exception("黑名單不得登入");
		}

		var userRes = UserRes.builder()
				.uid(userDetails.getUid())
				.username(userDetails.getUsername())
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