package com.zone.tasksphere.dto.response;

import com.zone.tasksphere.dto.request.TaskFilterParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@Schema(description = "Saved Filter Response")
public class SavedFilterResponse {
    @Schema(description = "Id", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Project id", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID projectId;

    @Schema(description = "Name", example = "Task quá hạn của tôi")
    private String name;

    @Schema(description = "Normalized task filter criteria")
    private TaskFilterParams filterCriteria;

    @Schema(description = "Created at", example = "2023-12-31T23:59:59Z")
    private Instant createdAt;

    @Schema(description = "Updated at", example = "2024-01-01T00:10:00Z")
    private Instant updatedAt;
}
