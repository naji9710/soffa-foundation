package io.soffa.foundation.spring.config.jobs;

import io.soffa.foundation.events.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jobrunr.jobs.lambdas.JobRequest;

@Data
@NoArgsConstructor
@AllArgsConstructor
class Job implements JobRequest {

    private static final long serialVersionUID = 3989785077755912004L;
    private String id;
    private String description;
    private Event event;

    @Override
    public Class<JobManager> getJobRequestHandler() {
        return JobManager.class;
    }
}
