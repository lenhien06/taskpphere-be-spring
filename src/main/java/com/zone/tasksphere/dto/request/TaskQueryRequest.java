package com.zone.tasksphere.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Stable request body for smart task querying")
public class TaskQueryRequest {

    @Valid
    @NotNull
    @Schema(description = "Normalized filter criteria")
    private TaskFilterParams filter;

    @Min(0)
    @Builder.Default
    @Schema(description = "Page number", example = "0")
    private Integer page = 0;

    @Min(1)
    @Max(100)
    @Builder.Default
    @Schema(description = "Page size", example = "20")
    private Integer size = 20;

    @Schema(description = "Sort field", example = "dueDate")
    private String sortBy;

    @Builder.Default
    @Schema(description = "Sort order", example = "asc", allowableValues = {"asc", "desc"})
    private String order = "asc";
}
