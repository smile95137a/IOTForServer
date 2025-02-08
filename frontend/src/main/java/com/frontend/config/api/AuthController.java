package com.frontend.config.api;

import java.util.stream.Collectors;
import com.frontend.config.jwt.JwtProvider;
import com.frontend.config.service.UserPrinciple;
import com.frontend.entity.role.Role;
import com.frontend.enums.RoleName;
import com.frontend.repo.RoleRepository;
import com.frontend.req.user.UserReq;
import com.frontend.res.user.UserRes;
import com.frontend.service.RoleService;
import com.frontend.service.UserService;
import com.frontend.utils.ResponseUtils;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
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

    private static final String LOGIN_TYPE_EMAIL = "email";
    private static final String LOGIN_TYPE_PHONE = "phone";
    
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final RoleRepository roleRepository;


    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserReq userReq) {

        UsernamePasswordAuthenticationToken authToken;

        if (StringUtils.equalsIgnoreCase(userReq.getType(), LOGIN_TYPE_EMAIL)) {
            if (StringUtils.isBlank(userReq.getEmail())) {
                log.warn("[Auth] Email 登入失敗: Email 為空");
                return ResponseEntity.badRequest().body(ResponseUtils.error(400, "Email 為必填", null));
            }
            log.info("[Auth] 嘗試使用 Email 登入: {}", userReq.getEmail());
            authToken = new UsernamePasswordAuthenticationToken(userReq.getEmail(), userReq.getPassword());

        } else if (StringUtils.equalsIgnoreCase(userReq.getType(), LOGIN_TYPE_PHONE)) {
            if (StringUtils.isBlank(userReq.getCountryCode()) || StringUtils.isBlank(userReq.getPhone())) {
                log.warn("[Auth] 手機登入失敗: 國碼或手機號碼為空");
                return ResponseEntity.badRequest().body(ResponseUtils.error(400, "手機號碼與國碼為必填", null));
            }
            String fullPhoneNumber = userReq.getCountryCode() + userReq.getPhone();
            log.info("[Auth] 嘗試使用手機登入: {}", fullPhoneNumber);
            authToken = new UsernamePasswordAuthenticationToken(fullPhoneNumber, userReq.getPassword());

        } else {
            log.warn("[Auth] 無效的登入類型: {}", userReq.getType());
            return ResponseEntity.badRequest().body(ResponseUtils.error(400, "無效的登入類型", null));
        }

        var authentication = authenticationManager.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        var userDetails = (UserPrinciple) authentication.getPrincipal();
        var jwt = jwtProvider.generateToken(userDetails);

        var roles = userDetails.getAuthorities().stream()
                .map(x -> new Role(RoleName.valueOf(x.getAuthority())))  // 轉換為 Role 物件
                .collect(Collectors.toSet());

        var userRes = UserRes.builder()
                .uid(userDetails.getUid())
                .email(userDetails.getEmail())
                .name(userDetails.getName())
                .countryCode(userDetails.getCountryCode())
                .phoneNumber(userDetails.getPhoneNumber())
                .roles(roles)
                .roleIds(roles.stream()                           // 使用 stream 將 Set 轉換為流
                        .map(Role::getId)        // 提取每個 Role 的 id
                        .collect(Collectors.toSet()))     // 收集為 Set
                .build();

        var result = JwtResponse.builder()
                .accessToken(jwt)
                .user(userRes)
                .build();

        log.info("[Auth] 登入成功: {}", userRes);
        return ResponseEntity.ok(ResponseUtils.success(result));
    }
}
