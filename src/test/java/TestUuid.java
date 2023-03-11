import com.namelessmc.java_api.NamelessAPI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class TestUuid {

    @Test
    void testUuidConversion() {
        UUID java = UUID.fromString("09948878-fe20-44e3-a072-42c39869dd1f");
        String website = "09948878fe2044e3a07242c39869dd1f";
        Assertions.assertEquals(NamelessAPI.javaUuidToWebsiteUuid(java), website);
        Assertions.assertEquals(NamelessAPI.websiteUuidToJavaUuid(website), java);
    }

}
