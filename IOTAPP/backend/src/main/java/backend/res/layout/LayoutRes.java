package backend.res.layout;

import java.time.LocalDateTime;
import java.util.List;

import com.mo.app.enums.LayoutSize;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LayoutRes {
	private Long id;
	private String uid;
	private LayoutSize size;
	private List<LayoutAreaRes> areas;
	private float width;
	private float height;
	private float ratio;
}
