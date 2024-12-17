package backend.entity.user;

import java.time.LocalDateTime;

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
@Table(name = "line_user_action_log")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LineUserActionLog {


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column
	private String channelUid;
	
	@Column
	private String lineUserId;

	@Column
	private String clickUid;

	@Column
	private LocalDateTime createDate;

}


