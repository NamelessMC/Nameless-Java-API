import java.net.MalformedURLException;
import java.net.URI;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.exception.NamelessException;

public class TestTls {

    @Test
    void checkTlsVersion() throws MalformedURLException, NamelessException {
        final NamelessAPI api = NamelessAPI.builder(URI.create("https://check-tls.akamai.io/").toURL(), "").build();
        final JsonObject response = api.requests().get("v1/tlsinfo.json");
        Assertions.assertEquals(response.get("tls_sni_status").getAsString(), "present");
        Assertions.assertEquals(response.get("tls_version").getAsString(), "tls1.3");
        Assertions.assertEquals(response.get("tls_sni_value").getAsString(), "check-tls.akamai.io");
    }

}
