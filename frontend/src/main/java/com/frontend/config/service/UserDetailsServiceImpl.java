package com.frontend.config.service;

import java.util.stream.Collectors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.frontend.entity.user.User;
import com.frontend.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        log.info("[UserDetailsService] 嘗試登入: {}", identifier);

        User userEntity;

        if (identifier.contains("@")) {
            log.info("[UserDetailsService] 偵測為 Email 登入: {}", identifier);
            userEntity = userRepository.findByEmail(identifier)
                    .orElseThrow(() -> {
                        log.warn("[UserDetailsService] 找不到 Email: {}", identifier);
                        return new UsernameNotFoundException("找不到 Email: " + identifier);
                    });
        } else if (identifier.startsWith("+")) {
            log.info("[UserDetailsService] 偵測為手機號碼登入: {}", identifier);
            String countryCode = extractCountryCode(identifier);
            String phoneNumber = extractPhoneNumber(identifier);
            log.info("[UserDetailsService] 提取國碼: {}, 手機號碼: {}", countryCode, phoneNumber);

            userEntity = userRepository.findByCountryCodeAndPhoneNumber(countryCode, phoneNumber)
                    .orElseThrow(() -> {
                        log.warn("[UserDetailsService] 找不到手機號碼: {}{}", countryCode, phoneNumber);
                        return new UsernameNotFoundException("找不到手機號碼: " + identifier);
                    });
        } else {
            log.warn("[UserDetailsService] 無效的登入標識: {}", identifier);
            throw new UsernameNotFoundException("無效的登入標識: " + identifier);
        }

        log.info("[UserDetailsService] 登入成功: UID={}, Email={}, Phone={}", 
                userEntity.getId(), userEntity.getEmail(), userEntity.getPhoneNumber());

        return createUserPrinciple(userEntity);
    }

    /**
     * 建立 UserPrinciple 物件，統一處理權限
     */
    private UserPrinciple createUserPrinciple(User userEntity) {
        log.info("[UserDetailsService] 建立 UserPrinciple 對象, 用戶 ID: {}", userEntity.getId());

        var authorities = userEntity.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName().name()))
                .collect(Collectors.toList());

        return UserPrinciple.builder()
                .id(userEntity.getId())
                .uid(userEntity.getUid())
                .email(userEntity.getEmail())
                .countryCode(userEntity.getCountryCode())
                .phoneNumber(userEntity.getPhoneNumber())
                .password(userEntity.getPassword())
                .name(userEntity.getName())
                .authorities(authorities)
                .build();
    }

    /**
     * 從完整手機號碼提取國碼
     */
    private String extractCountryCode(String fullPhone) {
        return fullPhone.replaceAll("([+0-9]{1,4})(\\d{6,15})", "$1"); 
    }

    /**
     * 從完整手機號碼提取純手機號
     */
    private String extractPhoneNumber(String fullPhone) {
        return fullPhone.replaceAll("([+0-9]{1,4})(\\d{6,15})", "$2"); 
    }
}
