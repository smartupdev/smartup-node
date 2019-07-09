package global.smartup.node.service;

import global.smartup.node.compoment.IdGenerator;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.mapper.FundFlowMapper;
import global.smartup.node.mapper.FundFlowTxMapper;
import global.smartup.node.po.FundFlow;
import global.smartup.node.po.FundFlowTx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class FundFlowService {

    private static final Logger log = LoggerFactory.getLogger(FundFlowService.class);

    @Autowired
    private FundFlowMapper fundFlowMapper;

    @Autowired
    private FundFlowTxMapper fundFlowTxMapper;

    @Autowired
    private IdGenerator idGenerator;

    public void addChargeSut(String txHash, boolean isSuccess, String userAddress, BigDecimal sut, BigDecimal fee) {
        FundFlow flow = new FundFlow();
        String flowId = idGenerator.getHexStringId();
        flow.setFlowId(flowId);
        flow.setCurrency(PoConstant.Currency.SUT);
        flow.setType(PoConstant.FundFlow.Type.ChargeSut);
        flow.setDirection(PoConstant.FundFlow.Direction.In);
        flow.setUserAddress(userAddress);
        flow.setAmount(sut);
        flow.setFee(fee);
        flow.setIsSuccess(isSuccess);
        flow.setCreateTime(new Date());
        fundFlowMapper.insert(flow);
        addTxHashLink(flowId, txHash);
    }

    public void addChargeEth(String txHash, boolean isSuccess, String userAddress, BigDecimal eth, BigDecimal fee) {
        FundFlow flow = new FundFlow();
        String flowId = idGenerator.getHexStringId();
        flow.setFlowId(flowId);
        flow.setCurrency(PoConstant.Currency.ETH);
        flow.setType(PoConstant.FundFlow.Type.ChargeEth);
        flow.setDirection(PoConstant.FundFlow.Direction.In);
        flow.setUserAddress(userAddress);
        flow.setAmount(eth);
        flow.setFee(fee);
        flow.setIsSuccess(isSuccess);
        flow.setCreateTime(new Date());
        fundFlowMapper.insert(flow);
        addTxHashLink(flowId, txHash);
    }

    public void addWithdrawSut(String txHash, boolean isSuccess, String userAddress, BigDecimal sut, BigDecimal fee) {
        FundFlow flow = new FundFlow();
        String flowId = idGenerator.getHexStringId();
        flow.setFlowId(flowId);
        flow.setCurrency(PoConstant.Currency.SUT);
        flow.setType(PoConstant.FundFlow.Type.WithdrawSut);
        flow.setDirection(PoConstant.FundFlow.Direction.Out);
        flow.setUserAddress(userAddress);
        flow.setAmount(sut);
        flow.setFee(fee);
        flow.setIsSuccess(isSuccess);
        flow.setCreateTime(new Date());
        fundFlowMapper.insert(flow);
        addTxHashLink(flowId, txHash);
    }

    public void addWithdrawEth(String txHash, boolean isSuccess, String userAddress, BigDecimal eth, BigDecimal fee) {
        FundFlow flow = new FundFlow();
        String flowId = idGenerator.getHexStringId();
        flow.setFlowId(flowId);
        flow.setCurrency(PoConstant.Currency.ETH);
        flow.setType(PoConstant.FundFlow.Type.WithdrawEth);
        flow.setDirection(PoConstant.FundFlow.Direction.Out);
        flow.setUserAddress(userAddress);
        flow.setAmount(eth);
        flow.setFee(fee);
        flow.setIsSuccess(isSuccess);
        flow.setCreateTime(new Date());
        fundFlowMapper.insert(flow);
        addTxHashLink(flowId, txHash);
    }


    public void addTxHashLink(String flowId, String txHash) {
        List<String> list = new ArrayList<>();
        list.add(txHash);
        addTxHashLink(flowId, list);
    }

    public void addTxHashLink(String flowId, List<String> txHash) {
        for (String tx : txHash) {
            FundFlowTx flowTx = new FundFlowTx();
            flowTx.setFlowId(flowId);
            flowTx.setTxHash(tx);
            fundFlowTxMapper.insert(flowTx);
        }
    }

}
