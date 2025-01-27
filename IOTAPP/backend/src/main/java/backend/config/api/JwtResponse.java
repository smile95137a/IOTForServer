package backend.config.api;

import java.io.Serializable;

import backend.res.user.UserRes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JwtResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	private String accessToken;
	
	private UserRes user;


}