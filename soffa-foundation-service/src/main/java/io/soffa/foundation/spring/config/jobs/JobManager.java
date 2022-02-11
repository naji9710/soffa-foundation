package io.soffa.foundation.spring.config.jobs;

import io.soffa.foundation.commons.IdGenerator;
import io.soffa.foundation.core.context.TenantHolder;
import io.soffa.foundation.core.messages.Message;
import io.soffa.foundation.core.operations.MessageHandler;
import lombok.AllArgsConstructor;
import org.jobrunr.configuration.JobRunrConfiguration;
import org.jobrunr.jobs.lambdas.JobRequestHandler;

@AllArgsConstructor
public class JobManager implements JobRequestHandler<Job> {

    private MessageHandler handler;
    private JobRunrConfiguration.JobRunrConfigurationResult jobRunr;

    public Job enqueue(String description, Message event) {
        Job job = new Job(IdGenerator.secureRandomId("job_"), event.getTenantId(), description, event);
        jobRunr.getJobRequestScheduler().enqueue(job);
        return job;
    }

    @Override
    public void run(Job job) {
        TenantHolder.set(job.getTenant());
        handler.onMessage(job.getMessage());
    }

}
