package org.nh.pharmacy.web.rest;


import org.nh.common.dto.TaskDetailResponseDTO;
import org.nh.jbpm.domain.dto.TaskDetailDTO;
import org.nh.jbpm.service.TaskRuleService;
import org.nh.pharmacy.jbpm.task.TaskDetailHandler;
import org.nh.pharmacy.util.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.OK;

@RequestMapping("/api")
@RestController
public class TaskDetailResource {

    private final Logger log = LoggerFactory.getLogger(TaskDetailResource.class);

    private final TaskDetailHandler taskDetailHandler;
    private final TaskRuleService taskRuleService;

    public TaskDetailResource(@Qualifier("TaskDetailHandler") TaskDetailHandler taskDetailHandler, TaskRuleService taskRuleService) {
        this.taskDetailHandler = taskDetailHandler;
        this.taskRuleService = taskRuleService;
    }

    @GetMapping("/task-details/external")
    public ResponseEntity<TaskDetailResponseDTO> getTaskDetaiResponseDTO(@RequestParam Long taskId, @RequestParam String processId) {
        log.debug("getTaskDetaiResponseDTO() taskId : {}, processId : {}", taskId, processId);
        String documentNumber = CommonUtil.getDocumentNumber(processId);
        TaskDetailDTO taskDetailDTO = taskRuleService.getTaskDetail(taskId, documentNumber);
        if (taskDetailDTO == null) {
            log.error("getTaskDetaiResponseDTO() task detail not found for task Id : {} ", taskId);
            return null;
        }
        TaskDetailResponseDTO taskDetailResponseDTO = taskDetailHandler.getTaskDetailResponse(taskDetailDTO);
        log.debug("getTaskDetaiResponseDTO() taskDetailResponseDTO : ", taskDetailResponseDTO);
        if (taskDetailResponseDTO == null) {
            log.error("task detail response not build for task id : {} ", taskDetailDTO.getTaskId());
            return null;
        }
        return new ResponseEntity<>(taskDetailResponseDTO, OK);
    }
}
