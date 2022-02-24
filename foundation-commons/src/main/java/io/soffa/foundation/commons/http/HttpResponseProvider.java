package io.soffa.foundation.commons.http;

import java.net.URL;
import java.util.List;
import java.util.Map;

public interface HttpResponseProvider {

    HttpResponse apply(URL url, Map<String, List<String>> headers);
}
