package org.nh.pharmacy.jbpm.task.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.nh.common.dto.TaskActionURLDTO;
import org.nh.common.dto.TaskDetailResponseDTO;
import org.nh.jbpm.domain.dto.TaskDetailDTO;
import org.nh.pharmacy.domain.Dispense;
import org.nh.pharmacy.jbpm.task.TaskDetailHandler;
import org.nh.pharmacy.service.DispenseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("DispenseTaskDetailHandler")
public class DispenseTaskDetailHandler implements TaskDetailHandler {

    private final Logger log = LoggerFactory.getLogger(DispenseTaskDetailHandler.class);

    private final DispenseService dispenseService;

    private final ObjectMapper objectMapper;

    public DispenseTaskDetailHandler(DispenseService dispenseService, ObjectMapper objectMapper) {
        this.dispenseService = dispenseService;
        this.objectMapper = objectMapper;
    }

    /*@Override
    public TaskDetailResponseDTO getTaskDetailResponse(TaskDetailRequestDTO taskDetailRequestDTO, TaskDetailDTO taskDetailDTO) {
        log.debug("getTaskDetailResponse() taskDetailRequestDTO := " + taskDetailRequestDTO + ", taskDetailDTO := " + taskDetailDTO);
        String query = "documentNumber.raw:" + taskDetailDTO.getDocumentNumber();
        List<Dispense> dispenseList = dispenseService.search(query, PageRequest.of(0,1)).getContent();
        if(dispenseList == null || dispenseList.isEmpty()) {
            log.error("Dispense document not found for documentNumber = {} ", taskDetailDTO.getDocumentNumber());
            return null;
        }
        TaskDetailResponseDTO taskDetailResponseDTO = new TaskDetailResponseDTO(taskDetailRequestDTO);
        taskDetailResponseDTO.setDocumentType("Dispense");
        taskDetailResponseDTO.setData(objectMapper.convertValue(dispenseList.get(0), Map.class));

        List<TaskActionURLDTO> taskActionURLDTOList = new ArrayList<>();

        taskActionURLDTOList.add(createClaimAction(taskDetailRequestDTO));
        taskActionURLDTOList.add(createClaimAndStartAction(taskDetailRequestDTO));
        taskActionURLDTOList.add(createStartAction(taskDetailRequestDTO));

        taskDetailResponseDTO.setActionURL(taskActionURLDTOList);

        log.debug("getTaskDetailResponse() taskDetailResponseDTO := " + taskDetailResponseDTO);
        return taskDetailResponseDTO;
    }*/

    @Override
    public TaskDetailResponseDTO getTaskDetailResponse(TaskDetailDTO taskDetailDTO) {
        log.debug("getTaskDetailResponse() taskDetailDTO := " + taskDetailDTO);
        String query = "documentNumber.raw:" + taskDetailDTO.getDocumentNumber();
        List<Dispense> dispenseList = dispenseService.search(query, PageRequest.of(0, 1)).getContent();
        if (dispenseList == null || dispenseList.isEmpty()) {
            log.error("Dispense document not found for documentNumber = {} ", taskDetailDTO.getDocumentNumber());
            return null;
        }
        TaskDetailResponseDTO taskDetailResponseDTO = new TaskDetailResponseDTO();
        taskDetailResponseDTO.setDocumentType("Dispense");
        taskDetailResponseDTO.setData(objectMapper.convertValue(dispenseList.get(0), Map.class));

        List<TaskActionURLDTO> taskActionURLDTOList = new ArrayList<>();

        taskActionURLDTOList.add(createClaimAction(taskDetailDTO.getTaskId().toString()));
        taskActionURLDTOList.add(createClaimAndStartAction(taskDetailDTO.getTaskId().toString()));
        taskActionURLDTOList.add(createStartAction(taskDetailDTO.getTaskId().toString()));

        taskDetailResponseDTO.setActionURL(taskActionURLDTOList);

        log.debug("getTaskDetailResponse() taskDetailResponseDTO := " + taskDetailResponseDTO);
        return taskDetailResponseDTO;
    }

    private TaskActionURLDTO createClaimAction(String taskId) {
        log.debug("createClaimAction method starts : TaskId : {}", taskId);
        TaskActionURLDTO taskActionURLDTO = new TaskActionURLDTO();
        taskActionURLDTO.setName("Claim");
        taskActionURLDTO.setUrl("/pharmacy/api/jbpm/task/claim");
        HashMap<String, String> params = new HashMap<>();
        params.put("taskId", taskId);
        taskActionURLDTO.setParams(params);
        log.debug("createClaimAndStartAction method end : taskActionURLDTO : {}", taskActionURLDTO);
        return taskActionURLDTO;
    }

    private TaskActionURLDTO createClaimAndStartAction(String taskId) {
        log.debug("createClaimAndStartAction method starts : TaskId : {}", taskId);
        TaskActionURLDTO taskActionURLDTO = new TaskActionURLDTO();
        taskActionURLDTO.setName("ClaimAndStart");
        taskActionURLDTO.setUrl("/pharmacy/api/jbpm/task/claim-start");
        HashMap<String, String> params = new HashMap<>();
        params.put("taskId", taskId);
        taskActionURLDTO.setParams(params);
        log.debug("createClaimAndStartAction method end : taskActionURLDTO : {}", taskActionURLDTO);
        return taskActionURLDTO;
    }

    private TaskActionURLDTO createStartAction(String taskId) {
        log.debug("createStartAction method starts : TaskId : {}", taskId);
        TaskActionURLDTO taskActionURLDTO = new TaskActionURLDTO();
        taskActionURLDTO.setName("Start");
        taskActionURLDTO.setUrl("/pharmacy/api/jbpm/task/start");
        HashMap<String, String> params = new HashMap<>();
        params.put("taskId", taskId);
        taskActionURLDTO.setParams(params);
        log.debug("createStartAction method end : taskActionURLDTO : {}", taskActionURLDTO);
        return taskActionURLDTO;
    }
}
