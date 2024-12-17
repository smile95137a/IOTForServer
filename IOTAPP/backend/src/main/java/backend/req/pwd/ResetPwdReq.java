package backend.req.pwd;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResetPwdReq implements Serializable {

	private static final long serialVersionUID = 1L;

	private String token;

	private String pwd;

}
