import com.google.gson.JsonObject;
import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.exception.NamelessException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

public class TestTls {

    @Test
    void checkTlsVersion() throws MalformedURLException, NamelessException {
        NamelessAPI api = NamelessAPI.builder(new URL("https://check-tls.akamai.io/"), "").build();
        JsonObject response = api.requests().get("v1/tlsinfo.json");
        Assertions.assertEquals(response.get("tls_sni_status").getAsString(), "present");
        Assertions.assertEquals(response.get("tls_version").getAsString(), "tls1.3");
        Assertions.assertEquals(response.get("tls_sni_value").getAsString(), "check-tls.akamai.io");
    }

}
