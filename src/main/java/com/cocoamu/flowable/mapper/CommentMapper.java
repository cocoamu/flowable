package com.cocoamu.flowable.mapper;

import com.cocoamu.flowable.dto.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface CommentMapper {

    /**
     * 通过流程实例id获取审批意见列表
     *
     * @param processInstanceId 流程实例id
     * @return
     */
    List<Comment> getCommentListByProcessId(String processInstanceId);

    /**
     * 根据流程实例id获取评论列表-额外根据任务id过滤指定任务的评论
     *
     * @param taskId 任务id
     * @return
     */
    List<Comment> getCommentListByTaskId(String taskId);

}
