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

    public void save(String name, String value) {
        Dict dict = dictMapper.selectByPrimaryKey(name);
        if (dict == null) {
            dict = new Dict();
            dict.setName(name);
            dict.setValue(value);
            dictMapper.insert(dict);
        } else {
            dict.setValue(value);
            dictMapper.updateByPrimaryKey(dict);
        }
    }

    public void del(String name) {
        dictMapper.deleteByPrimaryKey(name);
    }

    public BigInteger getParseBlockNumber() {
        Dict dict = dictMapper.selectByPrimaryKey(BlockNumber);
        Long l = Long.valueOf(dict.getValue());
        return BigInteger.valueOf(l);
    }

    public String query(String name) {
        Dict dict = dictMapper.selectByPrimaryKey(name);
        if (dict != null) {
            return dict.getValue();
        }
        return null;
    }




}
