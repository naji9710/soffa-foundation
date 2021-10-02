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

    public void enqueue(String description, Event event) {
        jobRunr.getJobRequestScheduler().enqueue(new Job(
            Generator.secureRandomId("job_"), description, event
        ));
    }

    @Override
    public void run(Job job) throws Exception {
        TenantHolder.set(job.getEvent().getTenantId());
        dispatcher.handle(job.getEvent());
    }

}
