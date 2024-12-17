package backend.entity.layout;

import java.time.LocalDateTime;
import java.util.List;

import com.mo.app.enums.LayoutSize;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "layout")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Layout {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String uid;
	
	@Column
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(length = 10)
	private LayoutSize size;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "layout_id")
	private List<LayoutArea> areas;

	@Column
	private LocalDateTime createDate;

	@Column
	private LocalDateTime updateDate;
}
