package com.frontend.req.template;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TemplateGroupReq {

	private String uid;
	private String name;
	private String description;
	private LocalDateTime startDate;
	private LocalDateTime endDate;

	private List<com.mo.app.req.template.TemplateReq> templates;
}
