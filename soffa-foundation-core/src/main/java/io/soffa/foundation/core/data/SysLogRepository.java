package io.soffa.foundation.core.data;

import io.soffa.foundation.core.data.entities.SysLog;

public interface SysLogRepository {

    void save(SysLog log);

    long count();

}
