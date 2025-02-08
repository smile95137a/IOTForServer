package com.frontend.enums;

public enum BannerStatus{
        AVAILABLE("上架"),       // 奖品上架
        UNAVAILABLE("下架");

        private final String description;

        BannerStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }