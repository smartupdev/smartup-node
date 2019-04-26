package global.smartup.node.controller;

import global.smartup.node.compoment.Validator;
import global.smartup.node.constant.LangHandle;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.po.Post;
import global.smartup.node.po.Reply;
import global.smartup.node.service.*;
import global.smartup.node.util.Checker;
import global.smartup.node.util.Pagination;
import global.smartup.node.util.Wrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Api(description = "讨论区")
@RestController
@RequestMapping("/api")
public class PostController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(PostController.class);

    @Autowired
    private Validator validator;

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @Autowired
    private ReplyService replyService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private MarketService marketService;

    @ApiOperation(value = "发布主题", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：type(root=系统讨论区, market=市场讨论区), marketAddress(如果type=root marketAddress为空), title, description\n" +
                        "返回：是否成功")
    @RequestMapping("/user/post/add")
    public Object add(HttpServletRequest request, Post post) {
        try {
            String userAddress = getLoginUserAddress(request);
            post.setUserAddress(userAddress);
            String err = validator.validate(post, Post.Add.class);
            if (err != null) {
                return Wrapper.alert(err);
            }
            if (!Checker.isAddress(post.getUserAddress())) {
                return Wrapper.alert(getLocaleMsg(LangHandle.AddressFormatError));
            }
            if (!userService.isExist(post.getUserAddress())) {
                return Wrapper.alert(getLocaleMsg(LangHandle.UserAddressNotExist));
            }
            if (!PoConstant.Post.Type.Root.equals(post.getType())
                    && !PoConstant.Post.Type.Market.equals(post.getType())) {
                return Wrapper.alert(getLocaleMsg(LangHandle.PostTypeError));
            }
            postService.create(post);
            return Wrapper.success();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "主题详情", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：postId\n" +
                        "返回：obj = {\n" +
                        "　postId, type, marketAddress, userAddress, title, description, createTime\n" +
                        "}")
    @RequestMapping("/post/one")
    public Object one(HttpServletRequest request, Long postId) {
        try {
            if (!postService.isExist(postId)) {
                return Wrapper.alert(getLocaleMsg(LangHandle.PostNotExist));
            }
            Post post = postService.query(postId);
            return Wrapper.success(post);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "查询主题列表", httpMethod = "POST", response = Wrapper.class,
            notes = "参数：type(root/market), marketAddress(如果type=root marketAddress为空), pageNumb, pageSize\n" +
                    "返回： obj = {\n" +
                    "list = [ {见/api/post/one}, {} ... ]\n" +
                    "}\n")
    @RequestMapping("/post/list")
    public Object list(HttpServletRequest request, String type, String marketAddress, Integer pageNumb, Integer pageSize) {
        try {
            String userAddress = getLoginUserAddress(request);
            Pagination page = postService.queryPage(userAddress, type, marketAddress, pageNumb, pageSize);
            return Wrapper.success(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "发布回复", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：postId, fatherId(可以空, 如果对个一个回复进行回复则需填写, 且只能回复一级), content\n" +
                        "返回：是否成功")
    @RequestMapping("/user/post/reply/add")
    public Object replyAdd(HttpServletRequest request, Reply reply) {
        try {
            String userAddress = getLoginUserAddress(request);
            reply.setUserAddress(userAddress);
            String err = validator.validate(reply, Reply.Add.class);
            if (err != null) {
                return Wrapper.alert(err);
            }
            if (!postService.isExist(reply.getPostId())) {
                return Wrapper.alert(getLocaleMsg(LangHandle.PostNotExist));
            }
            if (reply.getFatherId() != null) {
                if (!replyService.isExist(reply.getFatherId())) {
                    return Wrapper.alert(getLocaleMsg(LangHandle.ReplyNotExist));
                }
            }
            if (!Checker.isAddress(reply.getUserAddress())) {
                return Wrapper.alert(getLocaleMsg(LangHandle.AddressFormatError));
            }
            if (!userService.isExist(reply.getUserAddress())) {
                return Wrapper.alert(getLocaleMsg(LangHandle.UserAddressNotExist));
            }
            replyService.add(reply);
            return Wrapper.success();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "回复详情", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：replyId\n" +
                        "返回：obj = {\n" +
                        "　replyId, postId, fatherId, userAddress, content, createTime, \n" +
                        "　childrenPage = {\n" +
                        "　　list = [ {见此obj}, ... ]\n" +
                        "　}\n" +
                        "}")
    @RequestMapping("/post/reply/one")
    public Object replyOne(HttpServletRequest request, Long replyId) {
        try {
            if (!replyService.isExist(replyId)) {
                return Wrapper.alert(getLocaleMsg(LangHandle.ReplyNotExist));
            }
            return Wrapper.success(replyService.query(replyId));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "主题下回复列表", httpMethod = "POST", response = Wrapper.class,
            notes = "参数：postId, pageNumb, pageSize\n" +
                    "返回：obj = { list = [ {见/api/post/reply/one}, {} ... ] }")
    @RequestMapping("/post/reply/list")
    public Object replyList(HttpServletRequest request, Long postId, Integer pageNumb, Integer pageSize) {
        try {
            if (!postService.isExist(postId)) {
                return Wrapper.alert(getLocaleMsg(LangHandle.PostNotExist));
            }
            String userAddress = getLoginUserAddress(request);
            Pagination page = replyService.queryPage(userAddress, postId, pageNumb, pageSize);
            return Wrapper.success(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "回复下回复列表", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：fatherId, pageNumb, pageSize\n" +
                        "返回：obj = { list = [ {见/api/post/reply/one}, {} ... ] }")
    @RequestMapping("/post/reply/children/list")
    public Object replyChildrenList(HttpServletRequest request, Long fatherId, Integer pageNumb, Integer pageSize) {
        try {
            if (!replyService.isExist(fatherId)) {
                return Wrapper.alert(getLocaleMsg(LangHandle.ReplyNotExist));
            }
            Pagination page = replyService.queryChildren(fatherId, pageNumb, pageSize);
            return Wrapper.success(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "标记/删除标记  帖子/回复  喜欢/不喜欢", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：isMark(true 标记 / false 取消标记), type(post/reply), isLike(true/false), id\n" +
                        "返回：是否成功")
    @RequestMapping("/user/post/like")
    public Object like(HttpServletRequest request, boolean isMark, String type, boolean isLike, Long id) {
        try {
            String userAddress = getLoginUserAddress(request);
            if (PoConstant.Liked.Type.Post.equals(type)) {
                Post post = postService.query(id);
                if (post != null) {
                    if (isMark) {
                        likeService.addMark(userAddress, post.getMarketAddress(), type, isLike, String.valueOf(id));
                    } else {
                        likeService.delMark(userAddress, post.getMarketAddress(), type, String.valueOf(id));
                    }
                }
            } else if (PoConstant.Liked.Type.Reply.equals(type)) {
                Reply reply = replyService.query(id);
                if (reply != null) {
                    Post post = postService.query(reply.getPostId());
                    if (post != null) {
                        if (isMark) {
                            likeService.addMark(userAddress, post.getMarketAddress(), type, isLike, String.valueOf(id));
                        } else {
                            likeService.delMark(userAddress, post.getMarketAddress(), type, String.valueOf(id));
                        }
                    }
                }
            }
            return Wrapper.success();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

}
