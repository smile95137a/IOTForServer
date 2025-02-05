//package com.frontend.config.security;
//
//import com.frontend.repo.RoleRepository;
//import com.frontend.repo.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Optional;
//
//@Service
//@RequiredArgsConstructor
//public class CustomUserDetailsService implements UserDetailsService {
//
//	private final UserRepository userRepository;
//
//	private final RoleRepository roleRepository;
//
//	@Override
//	public UserDetails loadUserByUsername(String username) {
//		User user = userRepository.getUserByUserName(username);
//		Role role = roleRepository.findById(user.getRoleId());
//
//		List<SimpleGrantedAuthority> authorities = Optional.ofNullable(role)
//				.map(r -> List.of(new SimpleGrantedAuthority(r.getRoleName()))).orElse(List.of());
//
//		return mapUserToCustomUserDetails(user, authorities);
//	}
//
//	private CustomUserDetails mapUserToCustomUserDetails(User user, List<SimpleGrantedAuthority> authorities) {
//
//		return CustomUserDetails.builder().id(Long.valueOf(user.getId())).username(user.getUsername())
//				.password(user.getPassword()).name(user.getNickname()).email(user.getEmail()).authorities(authorities)
//				.build();
//	}
//}