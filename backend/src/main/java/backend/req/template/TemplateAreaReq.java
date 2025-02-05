package backend.req.template;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TemplateAreaReq {

	private String uid;
	private int x;
	private int y;
	private int width;
	private int height;
	private String label;
	private String type;
	private String text;
}
