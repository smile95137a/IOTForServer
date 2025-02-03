package src.main.java.com.common.config.message;

import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse<T> implements Serializable {

	private static final long serialVersionUID = 1L;
    private int code;
    private String message;
    private boolean success;
    private T data;
}