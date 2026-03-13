package com.tus.maintainx.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ApprovedWindowTimelineDTO {
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
