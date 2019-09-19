package global.smartup.node.controller;

import org.junit.Test;


public class IndexControllerTest {

    @Test
    public void testLanguage() throws Exception {

        OkHttpUtil.post("/test/language", null);

    }

}
