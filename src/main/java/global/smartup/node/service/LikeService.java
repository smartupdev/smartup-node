package global.smartup.node.service;

import global.smartup.node.constant.PoConstant;
import global.smartup.node.mapper.LikedMapper;
import global.smartup.node.po.Liked;
import global.smartup.node.po.Post;
import global.smartup.node.po.Reply;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LikeService {

    private static final Logger log = LoggerFactory.getLogger(LikeService.class);

    @Autowired
    private LikedMapper likedMapper;

    public void addMark(String userAddress, String marketId, String type, boolean isLike, String objMark) {
        Liked liked = new Liked();
        liked.setUserAddress(userAddress);
        liked.setMarketId(marketId);
        liked.setType(type);
        liked.setIsLike(isLike);
        liked.setObjectMark(objMark);
        liked.setCreateTime(new Date());
        likedMapper.insert(liked);
    }

    public void mod(Liked liked) {
        likedMapper.updateByPrimaryKey(liked);
    }

    public void delMark(String userAddress, String marketId, String type, String objectMark) {
        Liked cdt = new Liked();
        cdt.setUserAddress(userAddress);
        cdt.setMarketId(marketId);
        cdt.setObjectMark(objectMark);
        cdt.setType(type);
        likedMapper.delete(cdt);
    }

    public Liked queryLiked(String userAddress, String marketId, String type, String objMark) {
        Liked liked = new Liked();
        liked.setUserAddress(userAddress);
        liked.setMarketId(marketId);
        liked.setType(type);
        liked.setObjectMark(objMark);
        return likedMapper.selectOne(liked);
    }

    public void queryFillLikeForPost(String userAddress, String marketId, Post post) {
        if (StringUtils.isBlank(userAddress)) {
            return;
        }
        Liked liked = queryLiked(userAddress, marketId, PoConstant.Liked.Type.Post, String.valueOf(post.getPostId()));
        if (liked != null) {
            if (liked.getIsLike()) {
                post.setIsLiked(true);
            } else {
                post.setIsDisliked(true);
            }
        }
    }

    public void queryFillLikeForPosts(String userAddress, String marketId, List<Post> list) {
        if (StringUtils.isAnyBlank(userAddress, marketId) || list == null || list.size() <= 0) {
            return;
        }
        List<String> ids = list.stream().map(p -> String.valueOf(p.getPostId())).collect(Collectors.toList());
        List<Liked> likes = queryList(userAddress, marketId, PoConstant.Liked.Type.Post, ids);
        if (likes.size() <= 0) {
            return;
        }
        List<Long> ls = likes.stream().map(l -> l.getIsLike() ? Long.valueOf(l.getObjectMark()) : 0L).collect(Collectors.toList());
        List<Long> dls = likes.stream().map(l -> l.getIsLike() ? 0L : Long.valueOf(l.getObjectMark())).collect(Collectors.toList());
        list.forEach(p -> {
            if (ls.contains(p.getPostId())) {
                p.setIsLiked(true);
            }
            if (dls.contains(p.getPostId())) {
                p.setIsDisliked(true);
            }
        });
    }

    public void queryFillLikeForReplies(String userAddress, String marketId, List<Reply> list) {
        if (StringUtils.isAnyBlank(userAddress, marketId) || list == null || list.size() <= 0) {
            return;
        }
        List<String> ids = list.stream().map(p -> String.valueOf(p.getReplyId())).collect(Collectors.toList());
        List<Liked> likes = queryList(userAddress, marketId, PoConstant.Liked.Type.Reply, ids);
        if (likes.size() <= 0) {
            return;
        }
        List<Long> ls = likes.stream().map(l -> l.getIsLike() ? Long.valueOf(l.getObjectMark()) : 0L).collect(Collectors.toList());
        List<Long> dls = likes.stream().map(l -> l.getIsLike() ? 0L : Long.valueOf(l.getObjectMark())).collect(Collectors.toList());
        System.out.println("");
        list.forEach(r -> {
            if (ls.contains(r.getReplyId())) {
                r.setIsLiked(true);
            }
            if (dls.contains(r.getReplyId())) {
                r.setIsDisliked(true);
            }
        });
    }

    private List<Liked> queryList(String userAddress, String marketId, String type, List<String> ids) {
        Example example = new Example(Liked.class);
        example.createCriteria()
                .andEqualTo("userAddress", userAddress)
                .andEqualTo("marketId", marketId)
                .andEqualTo("type", type)
                .andIn("objectMark", ids);
        return likedMapper.selectByExample(example);
    }

}