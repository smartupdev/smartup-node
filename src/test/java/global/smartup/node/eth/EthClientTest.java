package global.smartup.node.eth;

import global.smartup.node.Starter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Starter.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EthClientTest {

    @Autowired
    private EthClient ethClient;

    @Test
    public void getTx() {
        ethClient.getTx("0xa13d9bf332e4f2de35f50618517d3718195c222b633b602057a6d8b86ba18e41");
    }

    @Test
    public void getTxReceipt() {
        ethClient.getTxReceipt("0xa13d9bf332e4f2de35f50618517d3718195c222b633b602057a6d8b86ba18e41");
    }

    @Test
    public void recoverSignature() {
        // 0x59b18e35a1e3ecbe10b3e874646a103213d9f98d01b7e35318be6af61d6747f6605800c8280c9adbdaf95dae913ac8f1fc46eef3aeaa259698d32658c599fdcd1b
        // Hi_i_am_string

        String address = "0xB44940Be0eeA81a3D0dA22CC15208AF4744BeA8E";
        String message = "8086083001000";
        String sign = "0xd8292beaaa42fee39a246d5372ac089a98574c8d9160118afe25d9c64badbee764be7e17384066610dd35d69642da8998edd51bf5e778f565bd313d7895b65791b";

        boolean ret = ethClient.recoverSignature(
                address,
                message,
                sign);
        System.out.println("recoverSignature : " + ret);
    }

}
