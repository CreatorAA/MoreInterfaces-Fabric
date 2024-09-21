package online.pigeonshouse.moreinterfaces.netty.message;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorMessage extends SimpleSessionMessage{
    public static final Integer MESSAGE_TYPE = 99;
    private Integer error_code;
    private String error;

    public ErrorMessage() {
        super(null, MESSAGE_TYPE);
    }

    public ErrorMessage(String sessionId, Integer error_code, String error) {
        super(sessionId, MESSAGE_TYPE);
        this.error_code = error_code;
        this.error = error;
    }
}
