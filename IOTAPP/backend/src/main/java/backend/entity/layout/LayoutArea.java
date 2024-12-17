package backend.entity.layout;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "layout_area")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LayoutArea {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String uid;

	@Column
	private int x;

	@Column
	private int y;

	@Column
	private int width;

	@Column
	private int height;


}
