package io.soffa.foundation.spring.config.jobs;

import io.soffa.foundation.actions.ActionDispatcher;
import io.soffa.foundation.context.TenantHolder;
import io.soffa.foundation.events.Event;
import io.soffa.foundation.support.Generator;
import lombok.AllArgsConstructor;
import org.jobrunr.configuration.JobRunrConfiguration;
import org.jobrunr.jobs.lambdas.JobRequestHandler;

@AllArgsConstructor
public class JobManager implements JobRequestHandler<Job> {

    private ActionDispatcher dispatcher;
    private JobRunrConfiguration.JobRunrConfigurationResult jobRunr;

    public Job enqueue(String description, Event event) {
        Job job = new Job(Generator.secureRandomId("job_"), event.getTenantId(), description, event);
        jobRunr.getJobRequestScheduler().enqueue(job);
        return job;
    }

    @Override
    public void run(Job job)   {
        TenantHolder.set(job.getTenant());
        dispatcher.handle(job.getEvent());
    }

}
