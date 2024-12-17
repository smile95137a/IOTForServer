package backend.res.layout;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LayoutAreaRes {
    private Long id;
    private String uid;
    private float x;
    private float y;
    private float width;
    private float height;
    private float ratio;
}