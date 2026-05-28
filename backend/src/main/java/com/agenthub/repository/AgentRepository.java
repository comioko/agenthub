package com.agenthub.repository;

import com.agenthub.model.entity.Agent;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AgentRepository extends BaseMapper<Agent> {
}
