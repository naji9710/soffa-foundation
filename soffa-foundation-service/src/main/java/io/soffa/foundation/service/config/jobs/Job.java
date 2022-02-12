package io.soffa.foundation.service.config.jobs;

import io.soffa.foundation.messages.Message;
import io.soffa.foundation.model.TenantId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jobrunr.jobs.lambdas.JobRequest;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Job implements JobRequest {

    private static final long serialVersionUID = 3989785077755912004L;
    private String id;
    private TenantId tenant;
    private String description;
    private Message message;

    @Override
    public Class<JobManager> getJobRequestHandler() {
        return JobManager.class;
    }

}
