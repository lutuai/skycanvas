package com.skycanvas.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.skycanvas.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论Mapper
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}

