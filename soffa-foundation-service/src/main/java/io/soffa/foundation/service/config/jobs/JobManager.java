package io.soffa.foundation.service.config.jobs;

/*
import io.soffa.foundation.commons.IdGenerator;
import io.soffa.foundation.context.TenantHolder;
import io.soffa.foundation.pubsub.Message;
import io.soffa.foundation.pubsub.MessageHandler;
import lombok.AllArgsConstructor;
import org.jobrunr.configuration.JobRunrConfiguration;
import org.jobrunr.jobs.lambdas.JobRequestHandler;


@AllArgsConstructor
public class JobManager implements JobRequestHandler<Job> {

    private MessageHandler handler;
    private JobRunrConfiguration.JobRunrConfigurationResult jobRunr;

    public Job enqueue(String description, Message event) {
        Job job = new Job(IdGenerator.secureRandomId("job_"), event.getContext().getTenantId(), description, event);
        jobRunr.getJobRequestScheduler().enqueue(job);
        return job;
    }

    @Override
    public void run(Job job) {
        TenantHolder.set(job.getTenant());
        handler.handle(job.getMessage());
    }

}
*/
