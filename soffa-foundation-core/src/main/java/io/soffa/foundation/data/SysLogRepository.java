package io.soffa.foundation.data;

import io.soffa.foundation.data.entities.SysLog;

public interface SysLogRepository {

    void save(SysLog log);

    long count();

}
