package backend.enums;

public enum NewsStatus {
    AVAILABLE("上架"),       // 奖品上架
    UNAVAILABLE("下架");

    private final String description;

    NewsStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
