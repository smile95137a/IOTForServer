package backend.req.menu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MenuTabLayoutAreaReq {
	
	private String uid;
	
	private String layoutUid;
	
	private Boolean isTabArea;
	
	private int x;

	private int y;

	private int width;

	private int height;

	private String label;

	private String type;

	private String text;
	
	private String switchRichMenu;
	
	private int orderNum;

	
}
