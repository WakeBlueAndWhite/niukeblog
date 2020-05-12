package com.ceer.niukeblog.controller;

import com.ceer.niukeblog.annotation.LoginRequired;
import com.ceer.niukeblog.entity.User;
import com.ceer.niukeblog.enums.FollowEnum;
import com.ceer.niukeblog.service.FollowService;
import com.ceer.niukeblog.service.LikeService;
import com.ceer.niukeblog.service.UserService;
import com.ceer.niukeblog.util.CommunityUtil;
import com.ceer.niukeblog.util.HostHolder;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * @ClassName UserController
 * @Description TODO
 * @Author ceer
 * @Date 2020/5/1 15:31
 * @Version 1.0
 */
@Slf4j
@Controller
@RequestMapping("/user")
public class UserController {

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${quniu.bucket.header.url}")
    private String headerBucketUrl;
    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;
    /**
     * @Description: 账号设置
     * @param:
     * @return:
     * @date: 2020/5/1 17:29
     */
    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage(Model model) {
        // 上传文件名称
        String fileName = CommunityUtil.generateUUID();
        // 设置响应信息
        StringMap policy = new StringMap();
        policy.put("returnBody", CommunityUtil.getJSONString(0));
        // 生成上传凭证
        Auth auth = Auth.create(accessKey, secretKey);
        String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, policy);

        model.addAttribute("uploadToken", uploadToken);
        model.addAttribute("fileName", fileName);

        return "/site/setting";
    }

    /**
     * @Description: 更新头像路径
     * @param:
     * @return:
     * @date: 2020/5/10 21:58
     */
    @RequestMapping(path = "/header/url", method = RequestMethod.POST)
    @ResponseBody
    public String updateHeaderUrl(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return CommunityUtil.getJSONString(1, "文件名不能为空!");
        }

        String url = headerBucketUrl + "/" + fileName;
        userService.updateHeader(hostHolder.getUser().getId(), url);

        return CommunityUtil.getJSONString(0);
    }
    /**
     * @Description: 上传文件 废弃
     * @param:
     * @return:
     * @date: 2020/5/1 17:29
     */
    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        //验证图片及其格式
        if (headerImage == null) {
            model.addAttribute("error", "您还没有上传图片！");
            return "/site/setting";
        }
        String filename = headerImage.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf("."));
        if (suffix == null) {
            model.addAttribute("error", "上传的文件格式不正确！");
        }

        //给图片生成随机名称
        filename = CommunityUtil.generateUUID() + suffix;
        //将文件存贮在本地
        File file = new File(uploadPath + "/" + filename);
        try {
            //存储文件
            headerImage.transferTo(file);
        } catch (IOException e) {
            log.error("上传图片失败：" + e.getMessage());
            throw new RuntimeException("上传文件失败,服务器发生异常!", e);
        }
        // 更新当前用户的头像的路径(web访问路径)
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    /**
     * @Description: 废弃
     * @param:
     * @return:
     * @date: 2020/5/1 15:57
     */
    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 服务器存放路径
        fileName = uploadPath + "/" + fileName;
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            log.error("读取头像失败: " + e.getMessage());
        }
    }

    @PostMapping("/updatePassword")
    public String updatePassword(@RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Model model) {
        if (!newPassword.equals(confirmPassword)){
            model.addAttribute("confirmPasswordMsg", "两次输入的密码不一致! ");
            return "/site/setting";
        }
        Map<String, Object> map = userService.checkPassword(oldPassword,hostHolder.getUser());
        if (map != null) {
            model.addAttribute("oldPasswordMsg", map.get("passwordMsg"));
            return "/site/setting";
        }
        String password = CommunityUtil.md5(newPassword + hostHolder.getUser().getSalt());
        userService.updatePassword(password,hostHolder.getUser().getId());
        return "redirect:/login";
    }

    /**
     * @Description: 用户个人主页
     * @param:
     * @return:
     * @date: 2020/5/4 20:30
     */
    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") Integer userId, Model model){
        User user = userService.selectByPrimaryKey(userId);
        if (user == null){
            throw new RuntimeException("该用户不存在!");
        }
        //登录者
        User loginUser = hostHolder.getUser();
        model.addAttribute("loginUser",loginUser);
        //用户信息
        model.addAttribute("user",user);
        //所获的总点赞数量
        Integer userLikeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",userLikeCount);
        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, FollowEnum.FOLLOW_USER.getType());
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(FollowEnum.FOLLOW_USER.getType(), userId);
        model.addAttribute("followerCount", followerCount);
        // 是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), FollowEnum.FOLLOW_USER.getType(), userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);
        return "/site/profile";
    }
}
