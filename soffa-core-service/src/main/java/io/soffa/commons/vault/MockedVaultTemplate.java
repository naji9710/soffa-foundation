package io.soffa.commons.vault;

import com.google.common.collect.ImmutableMap;
import org.mockito.Mockito;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;
import java.util.Map;

public class MockedVaultTemplate  {

    private final VaultTemplate template;

    public MockedVaultTemplate() {
        template = Mockito.mock(VaultTemplate.class);
    }

    public void put(String path, Map<String, Object> data) {
        VaultResponse response = new VaultResponse();
        response.setData(ImmutableMap.of("data", data));
        org.mockito.Mockito.when(template.read("secret/data/" + path.replaceAll("^/|/$", ""))).thenReturn(response);
    }

    public VaultTemplate getTemplate() {
        return template;
    }
}
