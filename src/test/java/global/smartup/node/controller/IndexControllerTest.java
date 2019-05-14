package global.smartup.node.controller;

import global.smartup.node.Starter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("unit")
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = Starter.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IndexControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testLanguage() throws Exception {

        mockMvc.perform(get("/test/language").header("sn-language", "zh-CN"))
                .andDo(print())
                .andExpect(status().isOk());

    }

}
