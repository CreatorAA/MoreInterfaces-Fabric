package online.pigeonshouse.moreinterfaces.netty.message;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotSessionErrorMessage implements Message {
    public static final Integer MESSAGE_TYPE = -1;
    private int error_code;
    private String error;

    public NotSessionErrorMessage() {
    }

    public NotSessionErrorMessage(int error_code, String error) {
        this.error_code = error_code;
        this.error = error;
    }

    @Override
    public Integer getType() {
        return MESSAGE_TYPE;
    }
}
