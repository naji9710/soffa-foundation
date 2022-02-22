package com.company.app.core;

import io.soffa.foundation.annotations.Query;
import io.soffa.foundation.application.Operation;

@Query
public interface Ping extends Operation<Void, PingResponse> {
}
