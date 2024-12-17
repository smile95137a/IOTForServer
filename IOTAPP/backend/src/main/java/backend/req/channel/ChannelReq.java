package backend.req.channel;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChannelReq implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String uid;
	private String name;
	private String lineToken;
	private String lineSecret;

}
