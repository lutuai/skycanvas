package com.skycanvas.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.skycanvas.entity.Work;
import org.apache.ibatis.annotations.Mapper;

/**
 * 作品Mapper
 */
@Mapper
public interface WorkMapper extends BaseMapper<Work> {
}

