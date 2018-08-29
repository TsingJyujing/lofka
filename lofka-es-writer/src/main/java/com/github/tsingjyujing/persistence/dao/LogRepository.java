package com.github.tsingjyujing.persistence.dao;

import com.github.tsingjyujing.persistence.pojo.LogEntity;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: zhuyh
 * @date: 2018/8/22
 */
public interface LogRepository extends ElasticsearchRepository<LogEntity,String> {

}
