package backend.req.user;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserReq implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String uid;

	private String username;

	private String password;

	private String name;

	private String email;

}
