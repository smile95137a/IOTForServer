package src.main.java.com.frontend.config.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomUserDetails implements UserDetails {

    private static final long serialVersionUID = 1L;
    
	private Long id;
    private String username;
    private String password;
    private String name;
    private String email;
    private String userUid;
    private String avatarUrl;
    private Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes;

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

