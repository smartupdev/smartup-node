package global.smartup.node.eth;

import global.smartup.node.Config;
import global.smartup.node.service.block.BlockService;
import global.smartup.node.service.DictService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.math.BigInteger;

@Component
public class BlockListener {

    private static final Logger log = LoggerFactory.getLogger(BlockListener.class);

    private static BigInteger ParseBlockNumber = null;

    @Autowired
    private Config config;

    @Autowired
    private EthClient ethClient;

    @Autowired
    private DictService dictService;

    @Autowired
    private BlockService blockService;

    public void start() {
        BigInteger current = ethClient.getLastBlockNumber();
        if (current == null) {
            return;
        }
        if (ParseBlockNumber == null) {
            if (config.profilesActive.equals("dev")) {
                ParseBlockNumber = current;
            } else {
                ParseBlockNumber = dictService.getParseBlockNumber();
            }
        }
        if (ParseBlockNumber.compareTo(current) > 0) {
            return;
        }

        log.info("Current parse block {}", ParseBlockNumber);
        EthBlock.Block block = ethClient.getBlockByNumber(ParseBlockNumber, true);
        if (block == null) {
            return;
        }

        // parse
        blockService.parseBlock(block);

        // add
        ParseBlockNumber = ParseBlockNumber.add(BigInteger.ONE);
        dictService.saveParseBlockNumber(ParseBlockNumber);
    }




}
