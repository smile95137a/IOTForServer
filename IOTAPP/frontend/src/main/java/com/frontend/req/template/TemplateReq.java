package com.frontend.req.template;

import java.util.List;

import com.frontend.req.template.TemplateAreaReq;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TemplateReq {

	private String uid;
	private String name;
	private String richMenuId;
	private String richMenuAliasName;
	private String richMenuAliasId;
	private String chatBarText;
	private boolean selected;
	private int templateWidth;
	private int templateHeight;
	private String templateImg;
	private List<TemplateAreaReq> areas;

}
