package com.frontend.config.security;

import com.frontend.entity.role.Role;
import com.frontend.entity.user.User;
import com.frontend.repo.RoleRepository;
import com.frontend.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	private final RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        // 获取用户
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 获取用户角色集合
        Set<Role> roles = user.getRoles();  // user.getRoles() 返回 Set<Role> 类型

        // 将角色转换为 GrantedAuthority 列表
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName().name()))  // 提取每个角色的名称并转换为 SimpleGrantedAuthority
                .collect(Collectors.toList());  // 收集成 List

        // 返回用户的详细信息，包括角色
        return mapUserToCustomUserDetails(user, authorities);
    }

	private CustomUserDetails mapUserToCustomUserDetails(User user, List<SimpleGrantedAuthority> authorities) {

		return CustomUserDetails.builder().id(Long.valueOf(user.getId())).username(user.getUsername())
				.password(user.getPassword()).name(user.getNickname()).email(user.getEmail()).authorities(authorities)
				.build();
	}
}