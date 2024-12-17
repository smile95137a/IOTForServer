package backend.res.channel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChannelRes {
    private String uid;
    private String name;
    private String lineToken;
    private String lineSecret;
}
