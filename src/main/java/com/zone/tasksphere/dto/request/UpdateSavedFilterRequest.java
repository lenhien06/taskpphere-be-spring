package com.zone.tasksphere.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Update Saved Filter Request")
public class UpdateSavedFilterRequest {

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Saved filter name", example = "Task đang làm của tôi")
    private String name;

    @Valid
    @NotNull
    @Schema(description = "Normalized task filter criteria")
    private TaskFilterParams filterCriteria;
}
