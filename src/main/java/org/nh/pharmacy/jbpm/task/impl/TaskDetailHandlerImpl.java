package org.nh.pharmacy.jbpm.task.impl;

import org.nh.common.dto.TaskDetailRequestDTO;
import org.nh.common.dto.TaskDetailResponseDTO;
import org.nh.jbpm.domain.dto.TaskDetailDTO;
import org.nh.pharmacy.jbpm.task.TaskDetailHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("TaskDetailHandler")
public class TaskDetailHandlerImpl implements TaskDetailHandler {

    private final Logger log = LoggerFactory.getLogger(TaskDetailHandlerImpl.class);

    private final DispenseTaskDetailHandler dispenseTaskDetailHandler;

    public TaskDetailHandlerImpl(DispenseTaskDetailHandler dispenseTaskDetailHandler) {
        this.dispenseTaskDetailHandler = dispenseTaskDetailHandler;
    }

    /*@Override
    public TaskDetailResponseDTO getTaskDetailResponse(TaskDetailRequestDTO taskDetailRequestDTO, TaskDetailDTO taskDetailDTO) {
        TaskDetailHandler taskDetailHandler = null;
        log.debug("getTaskDetailResponse() processName := " + taskDetailDTO.getProcessName());
        switch (taskDetailDTO.getProcessName()) {
            case "dispense_document_process":
                taskDetailHandler = dispenseTaskDetailHandler;
                break;
        }
        if(taskDetailHandler == null) {
            return null;
        }
        return taskDetailHandler.getTaskDetailResponse(taskDetailRequestDTO, taskDetailDTO);
    }*/

    @Override
    public TaskDetailResponseDTO getTaskDetailResponse(TaskDetailDTO taskDetailDTO) {
        TaskDetailHandler taskDetailHandler = null;
        log.debug("getTaskDetailResponse() processName := " + taskDetailDTO.getProcessName());
        switch (taskDetailDTO.getProcessName()) {
            case "dispense_document_process":
                taskDetailHandler = dispenseTaskDetailHandler;
                break;
        }
        if(taskDetailHandler == null) {
            return null;
        }
        return taskDetailHandler.getTaskDetailResponse(taskDetailDTO);
    }
}
