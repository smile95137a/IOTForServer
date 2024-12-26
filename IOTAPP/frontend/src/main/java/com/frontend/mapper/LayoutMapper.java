package com.frontend.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.frontend.entity.layout.Layout;
import com.frontend.entity.layout.LayoutArea;
import com.frontend.res.layout.LayoutAreaRes;
import com.frontend.res.layout.LayoutRes;
import org.springframework.stereotype.Component;


@Component
public class LayoutMapper {
	
	 private static final int SCALE_FACTOR = 1;

    public LayoutRes mapToLayoutRes(Layout layout) {
    	
		com.mo.app.enums.LayoutSize layoutSize = layout.getSize();
		float width = layoutSize.getWidth();
		float height = layoutSize.getHeight();
		
        return LayoutRes.builder()
                .id(layout.getId())
                .uid(layout.getUid())
                .size(layout.getSize())
                .areas(mapToLayoutAreaResList(layout.getAreas()))
                .width(width/SCALE_FACTOR) 
                .height(height/SCALE_FACTOR) 
                .ratio(SCALE_FACTOR)
                .build();
    }

    private LayoutAreaRes mapToLayoutAreaRes(LayoutArea layoutArea) {
        return LayoutAreaRes.builder()
                .id(layoutArea.getId())
                .uid(layoutArea.getUid())
                .x(layoutArea.getX()/SCALE_FACTOR)
                .y(layoutArea.getY()/SCALE_FACTOR)
                .width(layoutArea.getWidth()/SCALE_FACTOR)
                .height(layoutArea.getHeight()/SCALE_FACTOR)
                .ratio(SCALE_FACTOR)
                .build();
    }

    private List<LayoutAreaRes> mapToLayoutAreaResList(List<LayoutArea> layoutAreas) {
        return layoutAreas.stream()
                .map(this::mapToLayoutAreaRes)
                .collect(Collectors.toList());
    }
}
