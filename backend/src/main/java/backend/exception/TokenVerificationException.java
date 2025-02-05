package src.main.java.backend.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TokenVerificationException extends Exception {

    private static final long serialVersionUID = 1L;
    
    private int code;
    
    public TokenVerificationException(int code, String message) {
        super(message);
        this.code = code;
    }

}