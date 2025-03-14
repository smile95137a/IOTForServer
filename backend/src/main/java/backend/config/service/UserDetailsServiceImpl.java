package backend.config.service;

import java.util.Collection;
import java.util.stream.Collectors;

import backend.repo.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		var userEntity = userRepository
				.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("找不到使用者名稱:" + username));
		var authorities = userEntity.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getRoleName().toString()));
		return  UserPrinciple.builder()
				.id(userEntity.getId())
				.uid(userEntity.getUid())
				.username(userEntity.getUsername())
				.password(userEntity.getPassword())
				.name(userEntity.getName())
				.authorities((Collection<? extends GrantedAuthority>) authorities)
				.build();
	}
}