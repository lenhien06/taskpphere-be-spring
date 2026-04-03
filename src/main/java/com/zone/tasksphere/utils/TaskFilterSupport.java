package com.zone.tasksphere.utils;

import com.zone.tasksphere.dto.request.TaskFilterParams;
import com.zone.tasksphere.entity.enums.TaskPriority;
import com.zone.tasksphere.entity.enums.TaskStatus;
import com.zone.tasksphere.entity.enums.TaskType;
import com.zone.tasksphere.exception.BadRequestException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class TaskFilterSupport {

    private static final int DEFAULT_DUE_WITHIN_DAYS = 7;
    private static final int MAX_DUE_WITHIN_DAYS = 30;

    private TaskFilterSupport() {
    }

    public static TaskFilterParams fromQuery(
            UUID projectId,
            String keyword,
            TaskStatus status,
            List<TaskStatus> statuses,
            String assigneeId,
            Boolean myTasks,
            Boolean activeWork,
            UUID sprintId,
            TaskPriority priority,
            List<TaskPriority> priorities,
            TaskType type,
            List<TaskType> types,
            Boolean overdue,
            Boolean dueSoon,
            Integer dueWithinDays
    ) {
        return TaskFilterParams.builder()
                .projectId(projectId)
                .keyword(keyword)
                .assigneeId(assigneeId)
                .myTasks(myTasks)
                .activeWork(activeWork)
                .statuses(mergeSingleIntoList(status, statuses))
                .sprintId(sprintId)
                .priorities(mergeSingleIntoList(priority, priorities))
                .types(mergeSingleIntoList(type, types))
                .overdue(overdue)
                .dueSoon(dueSoon)
                .dueWithinDays(dueWithinDays)
                .build();
    }

    public static TaskFilterParams normalize(TaskFilterParams source) {
        TaskFilterParams params = source == null ? new TaskFilterParams() : source.toBuilder().build();
        params.setKeyword(trimToNull(params.getKeyword()));
        params.setAssigneeId(trimToNull(params.getAssigneeId()));
        params.setStatuses(distinctList(params.getStatuses()));
        params.setPriorities(distinctList(params.getPriorities()));
        params.setTypes(distinctList(params.getTypes()));

        if ("me".equalsIgnoreCase(params.getAssigneeId())) {
            params.setMyTasks(true);
            params.setAssigneeId(null);
        }

        if (Boolean.TRUE.equals(params.getMyTasks()) && params.getAssigneeId() != null) {
            throw new BadRequestException("Không thể dùng assigneeId khác cùng lúc với myTasks=true");
        }

        if (params.getAssigneeId() != null) {
            try {
                UUID.fromString(params.getAssigneeId());
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("assigneeId không hợp lệ");
            }
        }

        if (params.getDueWithinDays() != null) {
            if (params.getDueWithinDays() < 1 || params.getDueWithinDays() > MAX_DUE_WITHIN_DAYS) {
                throw new BadRequestException("dueWithinDays phải nằm trong khoảng 1-" + MAX_DUE_WITHIN_DAYS);
            }
            params.setDueSoon(true);
        } else if (Boolean.TRUE.equals(params.getDueSoon())) {
            params.setDueWithinDays(DEFAULT_DUE_WITHIN_DAYS);
        }

        return params;
    }

    public static TaskFilterParams resolveForQuery(TaskFilterParams source, UUID currentUserId) {
        TaskFilterParams params = normalize(source);
        if (Boolean.TRUE.equals(params.getMyTasks())) {
            params.setAssigneeId(currentUserId.toString());
        }
        return params;
    }

    public static void validateSavedFilter(TaskFilterParams params) {
        if (params == null || !hasMeaningfulCondition(params)) {
            throw new BadRequestException("filterCriteria phải chứa ít nhất một điều kiện lọc");
        }
    }

    public static boolean hasMeaningfulCondition(TaskFilterParams params) {
        if (params == null) {
            return false;
        }
        return params.getKeyword() != null
                || params.getAssigneeId() != null
                || Boolean.TRUE.equals(params.getMyTasks())
                || Boolean.TRUE.equals(params.getActiveWork())
                || Boolean.TRUE.equals(params.getOverdue())
                || Boolean.TRUE.equals(params.getDueSoon())
                || params.getDueWithinDays() != null
                || params.getSprintId() != null
                || !safeList(params.getStatuses()).isEmpty()
                || !safeList(params.getPriorities()).isEmpty()
                || !safeList(params.getTypes()).isEmpty();
    }

    private static <T> List<T> mergeSingleIntoList(T singleValue, List<T> values) {
        LinkedHashSet<T> merged = new LinkedHashSet<>(safeList(values));
        if (singleValue != null) {
            merged.add(singleValue);
        }
        return new ArrayList<>(merged);
    }

    private static <T> List<T> distinctList(List<T> values) {
        return new ArrayList<>(new LinkedHashSet<>(safeList(values).stream().filter(Objects::nonNull).toList()));
    }

    private static <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
