package com.zone.tasksphere.specification;

import com.zone.tasksphere.dto.request.TaskFilterParams;
import com.zone.tasksphere.entity.Task;
import com.zone.tasksphere.entity.enums.TaskStatus;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TaskSpecification {

    /**
     * P4-BE-03: Backlog filter — chỉ lấy task không thuộc sprint nào
     */
    public static Specification<Task> isBacklog() {
        return (root, query, cb) -> cb.isNull(root.get("sprint"));
    }

    /**
     * Build Specification từ TaskFilterParams (dùng trong phân trang)
     */
    public static Specification<Task> buildFilter(TaskFilterParams params) {
        return buildFilter(params, true);
    }

    public static Specification<Task> buildFilter(TaskFilterParams params, boolean rootOnly) {
        return (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("assignee", JoinType.LEFT);
                root.fetch("reporter", JoinType.LEFT);
                root.fetch("sprint", JoinType.LEFT);
                root.fetch("parentTask", JoinType.LEFT);
                query.distinct(true);
            }
            List<Predicate> predicates = new ArrayList<>();

            // Bắt buộc thuộc project này
            if (params.getProjectId() != null) {
                predicates.add(cb.equal(root.get("project").get("id"), params.getProjectId()));
            }

            if (rootOnly) {
                // Chỉ task gốc: sub-task không hiển thị ở board/danh sách; sau promote parent_task_id = null
                predicates.add(cb.isNull(root.get("parentTask")));
            }

            // Tìm kiếm theo title hoặc taskCode
            if (params.getKeyword() != null && !params.getKeyword().isBlank()) {
                String pattern = "%" + params.getKeyword().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("taskCode")), pattern)
                ));
            }

            if (params.getStatuses() != null && !params.getStatuses().isEmpty()) {
                predicates.add(root.get("taskStatus").in(params.getStatuses()));
            }

            if (Boolean.TRUE.equals(params.getActiveWork())) {
                predicates.add(root.get("taskStatus").in(TaskStatus.activeStatuses()));
            }

            if (params.getAssigneeId() != null) {
                try {
                    UUID assigneeUuid = UUID.fromString(params.getAssigneeId());
                    predicates.add(cb.equal(root.get("assignee").get("id"), assigneeUuid));
                } catch (IllegalArgumentException ignored) {
                    // "me" đã được resolve thành UUID thật ở service layer trước khi vào đây
                }
            }

            if (params.getSprintId() != null) {
                predicates.add(cb.equal(root.get("sprint").get("id"), params.getSprintId()));
            }

            if (params.getPriorities() != null && !params.getPriorities().isEmpty()) {
                predicates.add(root.get("priority").in(params.getPriorities()));
            }

            if (params.getTypes() != null && !params.getTypes().isEmpty()) {
                predicates.add(root.get("type").in(params.getTypes()));
            }

            // Lọc task quá hạn
            if (Boolean.TRUE.equals(params.getOverdue())) {
                predicates.add(cb.and(
                    cb.isNotNull(root.get("dueDate")),
                    cb.lessThan(root.get("dueDate"), LocalDate.now()),
                    cb.not(root.get("taskStatus").in(TaskStatus.terminalStatuses()))
                ));
            }

            if (Boolean.TRUE.equals(params.getDueSoon()) || params.getDueWithinDays() != null) {
                LocalDate today = LocalDate.now();
                LocalDate upperBound = today.plusDays(params.getDueWithinDays() != null ? params.getDueWithinDays() : 7);
                predicates.add(cb.isNotNull(root.get("dueDate")));
                predicates.add(cb.between(root.get("dueDate"), today, upperBound));
                predicates.add(cb.not(root.get("taskStatus").in(TaskStatus.terminalStatuses())));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
