package com.zone.tasksphere.dto.request;

import com.zone.tasksphere.entity.enums.TaskPriority;
import com.zone.tasksphere.entity.enums.TaskStatus;
import com.zone.tasksphere.entity.enums.TaskType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(description = "Normalized task filter params for list, backlog, calendar, timeline and saved filters")
public class TaskFilterParams {

    /** Được set bởi controller/service từ PathVariable */
    @Schema(description = "Project id", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID projectId;

    @Schema(description = "Keyword search on title or taskCode", example = "export")
    private String keyword;

    /**
     * UUID hoặc "me" ở query legacy. Sau normalize, service sẽ resolve "me"/myTasks
     * thành UUID thật của current user.
     */
    @Schema(description = "Explicit assignee id. Use myTasks=true for current user filters.", example = "550e8400-e29b-41d4-a716-446655440000")
    private String assigneeId;

    @Schema(description = "Shortcut smart filter: only tasks assigned to current authenticated user", example = "true")
    private Boolean myTasks;

    @Schema(description = "Shortcut smart filter: active work only (IN_PROGRESS, IN_REVIEW)", example = "true")
    private Boolean activeWork;

    @Schema(description = "Filter by multiple statuses", example = "[\"IN_PROGRESS\",\"IN_REVIEW\"]")
    @Builder.Default
    private List<TaskStatus> statuses = new ArrayList<>();

    @Schema(description = "Sprint id", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID sprintId;

    @Schema(description = "Filter by multiple priorities", example = "[\"HIGH\",\"CRITICAL\"]")
    @Builder.Default
    private List<TaskPriority> priorities = new ArrayList<>();

    @Schema(description = "Filter by multiple task types", example = "[\"TASK\",\"BUG\"]")
    @Builder.Default
    private List<TaskType> types = new ArrayList<>();

    /** Chỉ lấy tasks quá hạn */
    @Schema(description = "Overdue = dueDate < today AND status not terminal", example = "true")
    private Boolean overdue;

    /** Chỉ lấy tasks sắp đến hạn */
    @Schema(description = "Due soon shortcut. If true and dueWithinDays is null, backend uses 7 days.", example = "true")
    private Boolean dueSoon;

    @Schema(description = "Custom due-soon window in days", example = "5")
    private Integer dueWithinDays;
}
