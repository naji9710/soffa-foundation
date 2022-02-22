package com.company.app.core;

import io.soffa.foundation.annotations.Query;
import io.soffa.foundation.core.Operation;

@Query
public interface Ping extends Operation<Void, PingResponse> {
}
