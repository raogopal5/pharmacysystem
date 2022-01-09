package org.nh.pharmacy.jbpm.task;

import org.nh.common.dto.TaskDetailRequestDTO;
import org.nh.common.dto.TaskDetailResponseDTO;
import org.nh.jbpm.domain.dto.TaskDetailDTO;

public interface TaskDetailHandler {

   // TaskDetailResponseDTO getTaskDetailResponse(TaskDetailRequestDTO taskDetailRequestDTO, TaskDetailDTO taskDetailDTO);

    TaskDetailResponseDTO getTaskDetailResponse(TaskDetailDTO taskDetailDTO);
}
