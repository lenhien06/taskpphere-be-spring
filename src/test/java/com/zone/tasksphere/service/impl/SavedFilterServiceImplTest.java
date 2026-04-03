package com.zone.tasksphere.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zone.tasksphere.dto.request.CreateSavedFilterRequest;
import com.zone.tasksphere.dto.request.TaskFilterParams;
import com.zone.tasksphere.dto.request.UpdateSavedFilterRequest;
import com.zone.tasksphere.entity.Project;
import com.zone.tasksphere.entity.SavedFilter;
import com.zone.tasksphere.entity.User;
import com.zone.tasksphere.entity.enums.ProjectStatus;
import com.zone.tasksphere.entity.enums.ProjectVisibility;
import com.zone.tasksphere.entity.enums.SystemRole;
import com.zone.tasksphere.entity.enums.UserStatus;
import com.zone.tasksphere.exception.BadRequestException;
import com.zone.tasksphere.exception.NotFoundException;
import com.zone.tasksphere.repository.ProjectMemberRepository;
import com.zone.tasksphere.repository.ProjectRepository;
import com.zone.tasksphere.repository.SavedFilterRepository;
import com.zone.tasksphere.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SavedFilterServiceImplTest {

    @Mock
    private SavedFilterRepository savedFilterRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectMemberRepository memberRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SavedFilterServiceImpl service;

    @Test
    void createFilter_normalizesMyTasksBeforePersisting() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        Project project = project(projectId, currentUserId);
        User user = user(currentUserId);

        CreateSavedFilterRequest request = new CreateSavedFilterRequest();
        request.setName("  Task quá hạn của tôi  ");
        request.setFilterCriteria(TaskFilterParams.builder()
                .myTasks(true)
                .activeWork(true)
                .overdue(true)
                .build());

        when(memberRepository.existsByProjectIdAndUserId(projectId, currentUserId)).thenReturn(true);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(user));
        when(savedFilterRepository.countByProjectIdAndCreatedById(projectId, currentUserId)).thenReturn(0L);
        when(savedFilterRepository.existsByProjectIdAndCreatedByIdAndNameIgnoreCase(projectId, currentUserId, "Task quá hạn của tôi"))
                .thenReturn(false);
        when(objectMapper.writeValueAsString(any(TaskFilterParams.class))).thenReturn("{\"myTasks\":true}");
        when(savedFilterRepository.save(any(SavedFilter.class))).thenAnswer(invocation -> {
            SavedFilter saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            saved.setCreatedAt(Instant.parse("2026-03-27T10:00:00Z"));
            saved.setUpdatedAt(Instant.parse("2026-03-27T10:00:00Z"));
            return saved;
        });
        when(objectMapper.readValue("{\"myTasks\":true}", TaskFilterParams.class)).thenReturn(
                TaskFilterParams.builder()
                        .assigneeId(currentUserId.toString())
                        .myTasks(true)
                        .activeWork(true)
                        .overdue(true)
                        .build()
        );

        service.createFilter(projectId, request, currentUserId);

        ArgumentCaptor<TaskFilterParams> captor = ArgumentCaptor.forClass(TaskFilterParams.class);
        verify(objectMapper).writeValueAsString(captor.capture());
        assertThat(captor.getValue().getMyTasks()).isTrue();
        assertThat(captor.getValue().getAssigneeId()).isNull();
    }

    @Test
    void createFilter_rejectsEmptyCriteria() {
        UUID projectId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();

        CreateSavedFilterRequest request = new CreateSavedFilterRequest();
        request.setName("Bộ lọc trống");
        request.setFilterCriteria(new TaskFilterParams());

        when(memberRepository.existsByProjectIdAndUserId(projectId, currentUserId)).thenReturn(true);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project(projectId, currentUserId)));
        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(user(currentUserId)));

        assertThatThrownBy(() -> service.createFilter(projectId, request, currentUserId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("ít nhất một điều kiện");
    }

    @Test
    void updateFilter_rejectsAccessToAnotherUsersFilter() {
        UUID filterId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();

        UpdateSavedFilterRequest request = new UpdateSavedFilterRequest();
        request.setName("Mine");
        request.setFilterCriteria(TaskFilterParams.builder().activeWork(true).build());

        when(savedFilterRepository.findByIdAndCreatedById(filterId, currentUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateFilter(filterId, request, currentUserId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("không có quyền truy cập");
    }

    private Project project(UUID projectId, UUID ownerId) {
        Project project = Project.builder()
                .id(projectId)
                .name("TaskSphere")
                .projectKey("TS")
                .status(ProjectStatus.ACTIVE)
                .visibility(ProjectVisibility.PRIVATE)
                .owner(user(ownerId))
                .build();
        project.setCreatedAt(Instant.now());
        project.setUpdatedAt(Instant.now());
        return project;
    }

    private User user(UUID userId) {
        User user = User.builder()
                .id(userId)
                .email(userId + "@tasksphere.local")
                .fullName("User " + userId.toString().substring(0, 8))
                .passwordHash("secret")
                .systemRole(SystemRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return user;
    }
}
