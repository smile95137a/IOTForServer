package com.frontend.req.router;

public class CircuitControlRequest {
    private Long routerId;
    private Boolean targetStatus;  // true=開, false=關
    
    public Long getRouterId() { return routerId; }
    public void setRouterId(Long routerId) { this.routerId = routerId; }
    
    public Boolean getTargetStatus() { return targetStatus; }
    public void setTargetStatus(Boolean targetStatus) { this.targetStatus = targetStatus; }
}