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
        add(txHash, isSuccess, PoConstant.Currency.SUT, PoConstant.FundFlow.Type.ChargeSut, PoConstant.FundFlow.Direction.In, userAddress, sut, fee);
    }

    public void addChargeEth(String txHash, boolean isSuccess, String userAddress, BigDecimal eth, BigDecimal fee) {
        add(txHash, isSuccess, PoConstant.Currency.ETH, PoConstant.FundFlow.Type.ChargeEth, PoConstant.FundFlow.Direction.In, userAddress, eth, fee);
    }

    public void addWithdrawSut(String txHash, boolean isSuccess, String userAddress, BigDecimal sut, BigDecimal fee) {
        add(txHash, isSuccess, PoConstant.Currency.SUT, PoConstant.FundFlow.Type.WithdrawSut, PoConstant.FundFlow.Direction.Out, userAddress, sut, fee);
    }

    public void addWithdrawEth(String txHash, boolean isSuccess, String userAddress, BigDecimal eth, BigDecimal fee) {
        add(txHash, isSuccess, PoConstant.Currency.ETH, PoConstant.FundFlow.Type.WithdrawEth, PoConstant.FundFlow.Direction.Out, userAddress, eth, fee);
    }

    public void addCreateMarket(String txHash, boolean isSuccess, String userAddress, BigDecimal sut, BigDecimal fee) {
        add(txHash, isSuccess, PoConstant.Currency.SUT, PoConstant.FundFlow.Type.CreateMarket, PoConstant.FundFlow.Direction.Out, userAddress, sut, fee);
    }

    private void add(String txHash, boolean isSuccess, String currency, String type, String direction, String userAddress, BigDecimal amount, BigDecimal fee) {
        FundFlow flow = new FundFlow();
        String flowId = idGenerator.getHexStringId();
        flow.setFlowId(flowId);
        flow.setCurrency(currency);
        flow.setType(type);
        flow.setDirection(direction);
        flow.setUserAddress(userAddress);
        flow.setAmount(amount);
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
