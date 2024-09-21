package online.pigeonshouse.moreinterfaces.config;

import lombok.Data;

import java.util.Objects;

@Data
public class RemoteToken {
    String token;
    int power;

    public RemoteToken() {
    }

    public RemoteToken(String token, int power) {
        this.token = token;
        this.power = power;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RemoteToken that)) return false;
        return Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(token);
    }
}
