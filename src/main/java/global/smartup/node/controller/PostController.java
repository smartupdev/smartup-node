package global.smartup.node.controller;

import global.smartup.node.compoment.Validator;
import global.smartup.node.constant.LangHandle;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.po.Post;
import global.smartup.node.po.Reply;
import global.smartup.node.service.MarketService;
import global.smartup.node.service.PostService;
import global.smartup.node.service.ReplyService;
import global.smartup.node.service.UserService;
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
@RequestMapping("/api/post")
public class PostController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(PostController.class);

    @Autowired
    private Validator validator;

    @Autowired
    private MarketService marketService;

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @Autowired
    private ReplyService replyService;

    @ApiOperation(value = "发布主题", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：type(root=系统讨论区, market=市场讨论区), marketAddress(如果type=root marketAddress为空), userAddress, title, description\n" +
                        "返回：是否成功")
    @RequestMapping("/add")
    public Object add(HttpServletRequest request, Post post) {
        try {
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
    @RequestMapping("/one")
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
    @RequestMapping("/list")
    public Object list(HttpServletRequest request, String type, String marketAddress, Integer pageNumb, Integer pageSize) {
        try {
            Pagination page = postService.queryPage(type, marketAddress, pageNumb, pageSize);
            return Wrapper.success(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "发布回复", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：postId, fatherId(可以空, 如果对个一个回复进行回复则需填写, 且只能回复一级), userAddress, content\n" +
                        "返回：是否成功")
    @RequestMapping("/reply/add")
    public Object replyAdd(HttpServletRequest request, Reply reply) {
        try {
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
    @RequestMapping("/reply/one")
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
    @RequestMapping("/reply/list")
    public Object replyList(HttpServletRequest request, Long postId, Integer pageNumb, Integer pageSize) {
        try {
            if (!postService.isExist(postId)) {
                return Wrapper.alert(getLocaleMsg(LangHandle.PostNotExist));
            }
            Pagination page = replyService.queryPage(postId, pageNumb, pageSize);
            return Wrapper.success(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Wrapper.sysError();
        }
    }

    @ApiOperation(value = "回复下回复列表", httpMethod = "POST", response = Wrapper.class,
                notes = "参数：fatherId, pageNumb, pageSize\n" +
                        "返回：obj = { list = [ {见/api/post/reply/one}, {} ... ] }")
    @RequestMapping("/reply/children/list")
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

}
