package com.skycanvas.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.skycanvas.entity.Post;
import org.apache.ibatis.annotations.Mapper;

/**
 * 帖子Mapper
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {
}

