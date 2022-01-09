package org.nh.pharmacy.service.kafka.impl;

import org.nh.pharmacy.service.kafka.TaskDetailRequestKafkaService;
import org.springframework.stereotype.Service;

@Service
public class TaskDetailRequestKafkaServiceImpl implements TaskDetailRequestKafkaService {

   /* private final Logger log = LoggerFactory.getLogger(TaskDetailRequestKafkaServiceImpl.class);

    private final MessageChannel taskDetailResponseChannel;

    private final TaskRuleService taskRuleService;

    private final TaskDetailHandler taskDetailHandler;

    public TaskDetailRequestKafkaServiceImpl(@Qualifier(Channels.TASK_DETAIL_RESPONSE_OUTPUT) MessageChannel taskDetailResponseChannel,
                                             TaskRuleService taskRuleService,
                                             @Qualifier("TaskDetailHandler") TaskDetailHandler taskDetailHandler) {
        this.taskDetailResponseChannel = taskDetailResponseChannel;
        this.taskRuleService = taskRuleService;
        this.taskDetailHandler = taskDetailHandler;
    }

    @Override
    @ServiceActivator(inputChannel = Channels.TASK_DETAIL_REQUEST_INPUT)
    public void consumeTaskDetailRequest(TaskDetailRequestDTO taskDetailRequestDTO) {
        log.debug("consumeTaskDetailRequest() taskDetailRequestDTO := " + taskDetailRequestDTO);

        if(!taskDetailRequestDTO.getModule().equalsIgnoreCase("phr")) {
            log.info("consumeTaskDetailRequest() module name {} not supported", taskDetailRequestDTO.getModule());
            return;
        }

        TaskDetailDTO taskDetailDTO = taskRuleService.getTaskDetail(taskDetailRequestDTO.getTaskId());
        if(taskDetailDTO == null) {
            log.error("consumeTaskDetailRequest() task detail not found for task Id := " + taskDetailRequestDTO.getTaskId());
            return;
        }

        TaskDetailResponseDTO taskDetailResponseDTO = taskDetailHandler.getTaskDetailResponse(taskDetailRequestDTO, taskDetailDTO);

        log.debug("consumeTaskDetailRequest() taskDetailResponseDTO := " + taskDetailResponseDTO);

        if(taskDetailResponseDTO == null) {
            log.error("task detail response not build for task id := " + taskDetailDTO.getTaskId());
            return;
        }

        Message message = MessageBuilder.withPayload(taskDetailResponseDTO).build();
        taskDetailResponseChannel.send(message);
    }*/

}
