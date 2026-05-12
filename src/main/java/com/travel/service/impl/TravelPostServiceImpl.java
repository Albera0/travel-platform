package com.travel.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.dto.Result;
import com.travel.dto.ScrollResult;
import com.travel.dto.UserDTO;
import com.travel.entity.TravelPost;
import com.travel.entity.Follow;
import com.travel.entity.User;
import com.travel.mapper.TravelPostMapper;
import com.travel.service.ITravelPostService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.service.IFollowService;
import com.travel.service.IUserService;
import com.travel.utils.SystemConstants;
import com.travel.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.travel.utils.RedisConstants.DESTINATION_LIKED_KEY;
import static com.travel.utils.RedisConstants.FEED_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 */
@Service
public class TravelPostServiceImpl extends ServiceImpl<TravelPostMapper, TravelPost> implements ITravelPostService {

    @Resource
    private IUserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IFollowService followService;

    @Override
    public Result queryHotDestination(Integer current) {
        //根据用户查询
        Page<TravelPost> page = query()
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));

        //获取当前页数据
        List<TravelPost> records = page.getRecords();
        //查询用户
        records.forEach(travelPost -> {
            this.queryDestinationUser(travelPost);
            this.isDestinationLiked(travelPost);
        });
        return Result.ok(records);
    }

    @Override
    public Result queryDestinationById(Long id) {
        //1.查询destination
        TravelPost travelPost = getById(id);
        if (travelPost == null) {
            return Result.fail("用户不存在");
        }
        
        //2.查询destination有关用户
        queryDestinationUser(travelPost);
        //3.查询destination是否被点赞
        isDestinationLiked(travelPost);
        return Result.ok(travelPost);
    }

    private void isDestinationLiked(TravelPost travelPost) {
        //1.获取登录用户
        UserDTO userDTO = UserHolder.getUser();
        if (userDTO == null) {
            //用户未登录，无需查询是否点赞
            return;
        }
        Long userId = UserHolder.getUser().getId();

        //2.判断当前登录是否已经点赞
        String key = DESTINATION_LIKED_KEY + travelPost.getId();
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        travelPost.setIsLike(score != null);
    }

    @Override
    public Result likeDestination(Long id) {
        //1.获取登录用户
        Long userId = UserHolder.getUser().getId();

        //2.判断当前登录是否已经点赞
        String key = DESTINATION_LIKED_KEY + id;
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());

        if (score == null) {
            //3.如果未点赞
            //数据库点赞+1
            boolean isSuccess = update().setSql("liked = liked + 1").eq("id", id).update();
            //保存用户到redis的set集合
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().add(key, userId.toString(), System.currentTimeMillis());
            }

        }else{
            //4.如果已点赞
            //数据库点赞数-1
            boolean isSuccess = update().setSql("liked = liked - 1").eq("id", id).update();
            //把用户从redis的set集合移除
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().remove(key, userId.toString());
            }

        }

        return Result.ok();
    }

    @Override
    public Result queryDestinationLikes(Long id) {
        String key = DESTINATION_LIKED_KEY + id;
        //1.查询top5点赞用户
        Set<String> top5 = stringRedisTemplate.opsForZSet().range(key, 0, 4);
        if (top5 == null && top5.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }

        //2.解析出其中的用户id
        List<Long> ids = top5.stream().map(Long::valueOf).collect(Collectors.toList());
        String idStr = StrUtil.join(",", ids);

        //3.根据用户id查询用户
        List<UserDTO> users = userService.query().
                in("id", ids).
                last("ORDER BY FIELD(id, " + idStr + ")").list()
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());


        //4.返回
        return Result.ok(users);
    }

    @Override
    public Result saveDestination(TravelPost travelPost) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        travelPost.setUserId(user.getId());
        // 保存探店博文
        boolean isSuccess = save(travelPost);
        if (!isSuccess) {
            return  Result.fail("新增笔记失败！");
        }
        //查询笔记作者的粉丝
        List<Follow> follows = followService.query().eq("follow_user_id", user.getId()).list();
        //推送笔记id给所有粉丝
        for (Follow follow : follows) {
            Long userId = follow.getUserId();
            String key = FEED_KEY + userId;
            stringRedisTemplate.opsForZSet().add(key, travelPost.getId().toString(), System.currentTimeMillis());
        }
        // 返回id
        return Result.ok(travelPost.getId());
    }

    @Override
    public Result queryDestinationOfFollow(Long max, Integer offset) {
        //获取当前用户
        Long userId = UserHolder.getUser().getId();
        //查询收件箱
        String key = FEED_KEY + userId;
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(key, 0, max, offset, 2);
        if (typedTuples == null || typedTuples.isEmpty()) {
            return Result.ok();
        }
        //解析数据：destinationId, score（时间戳），offset
        List<Long> ids = new ArrayList<>(typedTuples.size());
        long minTime = 0;
        int os = 1;
        for (ZSetOperations.TypedTuple<String> typedTuple : typedTuples) {
            //获取id
            ids.add(Long.valueOf(typedTuple.getValue()));
            //获取分数
            long time = typedTuple.getScore().longValue();
            if (time == minTime) {
                os++;
            }else{
                minTime = time;
                os = 1;
            }
        }
        //根据id查询destination
        String idStr = StrUtil.join(",", ids);
        List<TravelPost> travelPosts = query().
                in("id", ids).
                last("ORDER BY FIELD(id, " + idStr + ")").list();
        for (TravelPost travelPost : travelPosts) {
            //查询destination有关用户
            queryDestinationUser(travelPost);
            //查询destination是否被点赞
            isDestinationLiked(travelPost);
        }

        //封装并返回
        ScrollResult r =  new ScrollResult();
        r.setList(travelPosts);
        r.setOffset(os);
        r.setMinTime(minTime);
        return Result.ok(r);
    }

    private void queryDestinationUser(TravelPost travelPost) {
        Long userID = travelPost.getUserId();
        User user = userService.getById(userID);
        travelPost.setName(user.getNickName());
        travelPost.setIcon(user.getIcon());
    }
}
