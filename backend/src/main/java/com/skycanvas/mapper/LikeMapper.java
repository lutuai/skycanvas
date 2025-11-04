package com.skycanvas.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.skycanvas.entity.Like;
import org.apache.ibatis.annotations.Mapper;

/**
 * 点赞Mapper
 */
@Mapper
public interface LikeMapper extends BaseMapper<Like> {
}

