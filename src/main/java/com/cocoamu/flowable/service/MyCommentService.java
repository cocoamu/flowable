package com.cocoamu.flowable.service;

import com.cocoamu.flowable.dto.Comment;
import com.cocoamu.flowable.vo.ReturnVo;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;

import java.util.List;

public interface MyCommentService {

    /**
     * 添加审批意见
     * @param comment
     * @return
     */
    ReturnVo addComment(Comment comment);

    /**
     * 获取某个流程实例所有审批意见
     * @param processId
     * @return
     */
    List<Comment> getCommentListByProcessId(String processId);

    /**
     * 获取某个任务审批意见
     * @param taskId
     * @return
     */
    List<Comment> getCommentListByTaskId(String taskId);


    ReturnVo addComment(TaskEntity taskEntity,Integer commentType);

}
