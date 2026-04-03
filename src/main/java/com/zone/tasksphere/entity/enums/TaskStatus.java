package com.zone.tasksphere.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public enum TaskStatus {
    TODO("To Do", "#D9D9D9", 1, false),
    IN_PROGRESS("In Progress", "#1677FF", 2, false),
    IN_REVIEW("In Review", "#FAAD14", 3, false),
    DONE("Done", "#52C41A", 4, true),
    CANCELLED("Cancelled", "#FF4D4F", 5, true);

    private final String displayName;
    private final String colorHex;
    private final int sortOrder;
    private final boolean isTerminal;

    public boolean canTransitionTo(TaskStatus next) {
        // Open transition model (Jira-like): allow moving between any statuses.
        return next != null;
    }

    public static Set<TaskStatus> activeStatuses() {
        return EnumSet.of(IN_PROGRESS, IN_REVIEW);
    }

    public static Set<TaskStatus> terminalStatuses() {
        return Arrays.stream(values())
                .filter(TaskStatus::isTerminal)
                .collect(java.util.stream.Collectors.toCollection(() -> EnumSet.noneOf(TaskStatus.class)));
    }
}
