package com.frontend.req;

import com.frontend.enums.NewsStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewsReq {
    private String title;
    private String content;
    private NewsStatus status;
}
