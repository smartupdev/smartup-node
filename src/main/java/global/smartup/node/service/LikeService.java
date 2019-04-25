package global.smartup.node.service;

import global.smartup.node.constant.PoConstant;
import global.smartup.node.mapper.LikedMapper;
import global.smartup.node.po.Liked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class LikeService {

    private static final Logger log = LoggerFactory.getLogger(LikeService.class);

    @Autowired
    private LikedMapper likedMapper;

    public void addPostLike(String userAddress, String marketAddress, Long postId) {
        delLike(userAddress, marketAddress, String.valueOf(postId));
        Liked liked = new Liked();
        liked.setUserAddress(userAddress);
        liked.setMarketAddress(marketAddress);
        liked.setType(PoConstant.Liked.Type.LikePost);
        liked.setObjectMark(String.valueOf(postId));
        liked.setCreateTime(new Date());
        likedMapper.insert(liked);
    }

    public void addPostDislike(String userAddress, String marketAddress, Long postId) {
        delLike(userAddress, marketAddress, String.valueOf(postId));
        Liked liked = new Liked();
        liked.setUserAddress(userAddress);
        liked.setMarketAddress(marketAddress);
        liked.setType(PoConstant.Liked.Type.DislikePost);
        liked.setObjectMark(String.valueOf(postId));
        liked.setCreateTime(new Date());
        likedMapper.insert(liked);
    }

    public void addReplyLike(String userAddress, String marketAddress, Long replyId) {
        delLike(userAddress, marketAddress, String.valueOf(replyId));
        Liked liked = new Liked();
        liked.setUserAddress(userAddress);
        liked.setMarketAddress(marketAddress);
        liked.setType(PoConstant.Liked.Type.LikeReply);
        liked.setObjectMark(String.valueOf(replyId));
        liked.setCreateTime(new Date());
        likedMapper.insert(liked);
    }

    public void addReplyDislike(String userAddress, String marketAddress, Long replyId) {
        delLike(userAddress, marketAddress, String.valueOf(replyId));
        Liked liked = new Liked();
        liked.setUserAddress(userAddress);
        liked.setMarketAddress(marketAddress);
        liked.setType(PoConstant.Liked.Type.DislikeReply);
        liked.setObjectMark(String.valueOf(replyId));
        liked.setCreateTime(new Date());
        likedMapper.insert(liked);
    }

    public void delLike(String userAddress, String marketAddress, String objectMark) {
        Liked cdt = new Liked();
        cdt.setUserAddress(userAddress);
        cdt.setMarketAddress(marketAddress);
        cdt.setObjectMark(objectMark);
        likedMapper.delete(cdt);
    }

}
