package global.smartup.node.service;

import global.smartup.node.mapper.DictMapper;
import global.smartup.node.po.Dict;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
public class DictService {

    private static final String BlockNumber = "block_number";

    @Autowired
    private DictMapper dictMapper;

    public void saveParseBlockNumber(BigInteger number) {
        Dict dict = dictMapper.selectByPrimaryKey(BlockNumber);
        if (dict != null) {
            dict.setValue(number.toString());
            dictMapper.updateByPrimaryKey(dict);
        } else {
            dict = new Dict();
            dict.setName(BlockNumber);
            dict.setValue(number.toString());
            dictMapper.insert(dict);
        }
    }

    public BigInteger getParseBlockNumber() {
        Dict dict = dictMapper.selectByPrimaryKey(BlockNumber);
        Long l = Long.valueOf(dict.getValue());
        return BigInteger.valueOf(l);
    }

}
