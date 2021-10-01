package io.soffa.foundation.data;

public interface SysLogRepository {

    void save(SysLog log);

    long count();

}
