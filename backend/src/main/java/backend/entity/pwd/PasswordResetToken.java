package backend.entity.pwd;

import java.time.LocalDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
@Table(name = "PasswordResetTokens")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PasswordResetToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    @Column
    private Long userId;

    @Column
    private String token;

    @Column
    @Builder.Default
    private Boolean isActive = false;

    @Column
    @Builder.Default
    private Boolean passwordChanged = false;

	@Column
	private LocalDateTime createTime;
	
	@Column
	private LocalDateTime updateTime;


    @Column
    private LocalDateTime expireTime;
  
}
