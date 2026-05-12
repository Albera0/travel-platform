package com.travel.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.dto.Result;
import com.travel.dto.UserDTO;
import com.travel.entity.TravelPost;
import com.travel.service.ITravelPostService;
import com.travel.utils.SystemConstants;
import com.travel.utils.UserHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 */
@RestController
@RequestMapping("/travel-post")
public class TravelPostController {

    @Resource
    private ITravelPostService blogService;


    @PostMapping
    public Result saveDestination(@RequestBody TravelPost travelPost) {
        return blogService.saveDestination(travelPost);
    }

    @PutMapping("/like/{id}")
    public Result likeDestination(@PathVariable("id") Long id) {
        return blogService.likeDestination(id);
    }

    @GetMapping("/of/me")
    public Result queryMyDestination(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        // 根据用户查询
        Page<TravelPost> page = blogService.query()
                .eq("user_id", user.getId()).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<TravelPost> records = page.getRecords();
        return Result.ok(records);
    }

    @GetMapping("/hot")
    public Result queryHotDestination(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return blogService.queryHotDestination(current);
    }

    @GetMapping("/{id}")
    public Result queryDestinationById(@PathVariable("id") Long id) {
        return blogService.queryDestinationById(id);
    }

    @GetMapping("/likes/{id}")
    public Result queryDestinationLikes(@PathVariable("id") Long id) {
        return blogService.queryDestinationLikes(id);
    }

    // TravelPostController
    @GetMapping("/of/user")
    public Result queryDestinationByUserId(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam("id") Long id) {
        // 根据用户查询
        Page<TravelPost> page = blogService.query()
                .eq("user_id", id).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<TravelPost> records = page.getRecords();
        return Result.ok(records);
    }

    @GetMapping("/of/follow")
    public Result queryDestinationByFollowId(
            @RequestParam("lastId") Long max, @RequestParam(value = "offset", defaultValue = "0") Integer offset) {
        return blogService.queryDestinationOfFollow(max, offset);
    }
}
