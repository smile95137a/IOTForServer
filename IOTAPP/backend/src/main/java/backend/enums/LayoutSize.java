package com.mo.app.enums;

import lombok.Getter;

@Getter
public enum LayoutSize {
    FULL(2500, 1686),
    HALF(2500, 843);

    private final int width;
    private final int height;

    LayoutSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

}
