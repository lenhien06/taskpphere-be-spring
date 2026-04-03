package com.zone.tasksphere.utils;

import com.zone.tasksphere.dto.request.TaskFilterParams;
import com.zone.tasksphere.entity.enums.TaskPriority;
import com.zone.tasksphere.entity.enums.TaskStatus;
import com.zone.tasksphere.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskFilterSupportTest {

    @Test
    void normalize_supportsLegacyMeAliasAndSmartFlags() {
        TaskFilterParams params = TaskFilterSupport.normalize(TaskFilterSupport.fromQuery(
                UUID.randomUUID(),
                " export ",
                TaskStatus.IN_PROGRESS,
                List.of(TaskStatus.IN_REVIEW),
                "me",
                null,
                true,
                null,
                TaskPriority.HIGH,
                List.of(TaskPriority.CRITICAL),
                null,
                null,
                true,
                true,
                null
        ));

        assertThat(params.getKeyword()).isEqualTo("export");
        assertThat(params.getMyTasks()).isTrue();
        assertThat(params.getAssigneeId()).isNull();
        assertThat(params.getActiveWork()).isTrue();
        assertThat(params.getOverdue()).isTrue();
        assertThat(params.getDueWithinDays()).isEqualTo(7);
        assertThat(params.getStatuses()).containsExactlyInAnyOrder(TaskStatus.IN_PROGRESS, TaskStatus.IN_REVIEW);
        assertThat(params.getPriorities()).containsExactlyInAnyOrder(TaskPriority.HIGH, TaskPriority.CRITICAL);
    }

    @Test
    void resolveForQuery_mapsMyTasksToCurrentUser() {
        UUID currentUserId = UUID.randomUUID();

        TaskFilterParams params = TaskFilterSupport.resolveForQuery(TaskFilterParams.builder()
                .myTasks(true)
                .activeWork(true)
                .overdue(true)
                .build(), currentUserId);

        assertThat(params.getAssigneeId()).isEqualTo(currentUserId.toString());
        assertThat(params.getMyTasks()).isTrue();
    }

    @Test
    void normalize_rejectsExplicitAssigneeTogetherWithMyTasks() {
        assertThatThrownBy(() -> TaskFilterSupport.normalize(TaskFilterParams.builder()
                .myTasks(true)
                .assigneeId(UUID.randomUUID().toString())
                .build()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("myTasks=true");
    }

    @Test
    void validateSavedFilter_rejectsEmptyConditions() {
        assertThatThrownBy(() -> TaskFilterSupport.validateSavedFilter(new TaskFilterParams()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("ít nhất một điều kiện");
    }
}
