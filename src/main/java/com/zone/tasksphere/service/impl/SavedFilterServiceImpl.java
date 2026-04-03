package com.zone.tasksphere.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zone.tasksphere.dto.request.CreateSavedFilterRequest;
import com.zone.tasksphere.dto.request.TaskFilterParams;
import com.zone.tasksphere.dto.request.UpdateSavedFilterRequest;
import com.zone.tasksphere.dto.response.SavedFilterResponse;
import com.zone.tasksphere.entity.Project;
import com.zone.tasksphere.entity.SavedFilter;
import com.zone.tasksphere.entity.User;
import com.zone.tasksphere.exception.BadRequestException;
import com.zone.tasksphere.exception.BusinessRuleException;
import com.zone.tasksphere.exception.Forbidden;
import com.zone.tasksphere.exception.NotFoundException;
import com.zone.tasksphere.repository.ProjectMemberRepository;
import com.zone.tasksphere.repository.ProjectRepository;
import com.zone.tasksphere.repository.SavedFilterRepository;
import com.zone.tasksphere.repository.UserRepository;
import com.zone.tasksphere.service.SavedFilterService;
import com.zone.tasksphere.utils.TaskFilterSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SavedFilterServiceImpl implements SavedFilterService {

    private static final int MAX_FILTERS_PER_PROJECT = 10;

    private final SavedFilterRepository savedFilterRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    // ════════════════════════════════════════
    // POST /api/v1/projects/{projectId}/saved-filters
    // ════════════════════════════════════════
    @Override
    public SavedFilterResponse createFilter(UUID projectId, CreateSavedFilterRequest request, UUID currentUserId) {
        validateMembership(projectId, currentUserId);
        Project project = getProject(projectId);
        User user = getUser(currentUserId);
        TaskFilterParams normalizedFilter = normalizeFilter(request.getFilterCriteria());

        // Check giới hạn 10 filter/project/user
        long count = savedFilterRepository.countByProjectIdAndCreatedById(projectId, currentUserId);
        if (count >= MAX_FILTERS_PER_PROJECT) {
            throw new BusinessRuleException("Tối đa " + MAX_FILTERS_PER_PROJECT + " bộ lọc mỗi dự án");
        }

        if (savedFilterRepository.existsByProjectIdAndCreatedByIdAndNameIgnoreCase(projectId, currentUserId, request.getName().trim())) {
            throw new BadRequestException("Tên bộ lọc đã tồn tại trong dự án");
        }

        // Serialize filterCriteria → JSON string
        String criteriaJson;
        try {
            criteriaJson = objectMapper.writeValueAsString(normalizedFilter);
        } catch (Exception e) {
            throw new BusinessRuleException("filterCriteria không hợp lệ");
        }

        SavedFilter filter = SavedFilter.builder()
                .project(project)
                .createdBy(user)
                .name(request.getName().trim())
                .filterCriteria(criteriaJson)
                .isPublic(false)
                .build();

        filter = savedFilterRepository.save(filter);
        log.info("SavedFilter '{}' created by {} in project {}", filter.getName(), currentUserId, projectId);

        return toResponse(filter);
    }

    // ════════════════════════════════════════
    // GET /api/v1/projects/{projectId}/saved-filters
    // ════════════════════════════════════════
    @Override
    @Transactional(readOnly = true)
    public List<SavedFilterResponse> getFilters(UUID projectId, UUID currentUserId) {
        validateMembership(projectId, currentUserId);
        return savedFilterRepository
                .findByProjectIdAndCreatedByIdOrderByCreatedAtDesc(projectId, currentUserId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public SavedFilterResponse updateFilter(UUID filterId, UpdateSavedFilterRequest request, UUID currentUserId) {
        SavedFilter filter = savedFilterRepository.findByIdAndCreatedById(filterId, currentUserId)
                .orElseThrow(() -> new NotFoundException("Bộ lọc không tồn tại hoặc bạn không có quyền truy cập: " + filterId));

        UUID projectId = filter.getProject().getId();
        validateMembership(projectId, currentUserId);
        TaskFilterParams normalizedFilter = normalizeFilter(request.getFilterCriteria());

        if (savedFilterRepository.existsByProjectIdAndCreatedByIdAndNameIgnoreCaseAndIdNot(
                projectId, currentUserId, request.getName().trim(), filterId)) {
            throw new BadRequestException("Tên bộ lọc đã tồn tại trong dự án");
        }

        try {
            filter.setFilterCriteria(objectMapper.writeValueAsString(normalizedFilter));
        } catch (Exception e) {
            throw new BusinessRuleException("filterCriteria không hợp lệ");
        }
        filter.setName(request.getName().trim());

        SavedFilter updated = savedFilterRepository.save(filter);
        log.info("SavedFilter '{}' updated by {} in project {}", updated.getName(), currentUserId, projectId);
        return toResponse(updated);
    }

    // ════════════════════════════════════════
    // DELETE /api/v1/saved-filters/{filterId}
    // ════════════════════════════════════════
    @Override
    public void deleteFilter(UUID filterId, UUID currentUserId) {
        SavedFilter filter = savedFilterRepository.findByIdAndCreatedById(filterId, currentUserId)
                .orElseThrow(() -> new NotFoundException("Bộ lọc không tồn tại hoặc bạn không có quyền truy cập: " + filterId));

        UUID projectId = filter.getProject().getId();
        validateMembership(projectId, currentUserId);

        filter.setDeletedAt(Instant.now());
        savedFilterRepository.save(filter);
        log.info("SavedFilter {} deleted by {}", filterId, currentUserId);
    }

    // ════════════════════════════════════════
    // PRIVATE HELPERS
    // ════════════════════════════════════════

    private void validateMembership(UUID projectId, UUID userId) {
        if (!memberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new Forbidden("Bạn không phải thành viên dự án này");
        }
    }

    private Project getProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project không tồn tại: " + projectId));
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User không tồn tại: " + userId));
    }

    private SavedFilterResponse toResponse(SavedFilter filter) {
        TaskFilterParams criteria;
        try {
            criteria = objectMapper.readValue(filter.getFilterCriteria(), TaskFilterParams.class);
        } catch (Exception e) {
            log.warn("Failed to deserialize filterCriteria for filter {}: {}", filter.getId(), e.getMessage());
            criteria = new TaskFilterParams();
        }

        return SavedFilterResponse.builder()
                .id(filter.getId())
                .projectId(filter.getProject().getId())
                .name(filter.getName())
                .filterCriteria(criteria)
                .createdAt(filter.getCreatedAt())
                .updatedAt(filter.getUpdatedAt())
                .build();
    }

    private TaskFilterParams normalizeFilter(TaskFilterParams filterCriteria) {
        TaskFilterParams normalized = TaskFilterSupport.normalize(filterCriteria);
        TaskFilterSupport.validateSavedFilter(normalized);
        return normalized;
    }
}
