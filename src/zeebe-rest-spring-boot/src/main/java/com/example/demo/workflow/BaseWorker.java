package com.example.demo.workflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import org.springframework.stereotype.Component;

@Component
public interface BaseWorker {

    //@ZeebeRetryable
    @ZeebeWorker(type = "classify")
    void classify(final JobClient client, final ActivatedJob job);

    @ZeebeWorker(type = "classify-rollout")
    default void classifyRollout(final JobClient client, final ActivatedJob job) throws JsonProcessingException {}

}
