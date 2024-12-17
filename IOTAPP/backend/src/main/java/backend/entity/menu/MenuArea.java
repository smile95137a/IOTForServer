package backend.entity.menu;

import com.mo.app.enums.AreaType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "menu_area")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MenuArea {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String uid;

	@Column
	private String layoutUid;

	@Column
	private Boolean isTabArea;

	@Column
	private int x;

	@Column
	private int y;

	@Column
	private int width;

	@Column
	private int height;

	@Column
	private String label;

	@Enumerated(EnumType.STRING)
	@Column
	private AreaType type;

	@Column
	private String text;

	@Column
	private String switchRichMenu;

	@Column
	@Builder.Default
	private int clickCount = 0;

	@Column
	private int orderNum;

}
