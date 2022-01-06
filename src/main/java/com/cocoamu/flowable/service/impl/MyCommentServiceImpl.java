package com.cocoamu.flowable.service.impl;

import com.cocoamu.flowable.cmd.AddHisCommentCmd;
import com.cocoamu.flowable.dto.Comment;
import com.cocoamu.flowable.mapper.CommentMapper;
import com.cocoamu.flowable.service.MyCommentService;
import com.cocoamu.flowable.vo.ReturnVo;
import org.flowable.engine.ManagementService;
import org.flowable.engine.TaskService;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MyCommentServiceImpl implements MyCommentService {
    @Autowired
    private TaskService taskService;
    @Autowired
    private ManagementService managementService;
    @Autowired
    private CommentMapper commentMapper;

    @Override
    public ReturnVo addComment(Comment comment) {
        managementService.executeCommand(new AddHisCommentCmd(comment.getTaskId(), comment.getUserId(), comment.getProcessInstanceId(),
                comment.getType(), comment.getMessage()));
        return ReturnVo.sucess("添加评论成功");
    }

    @Override
    public List<Comment> getCommentListByProcessId(String processId) {
        return commentMapper.getCommentListByProcessId(processId);
    }

    @Override
    public List<Comment> getCommentListByTaskId(String taskId) {
        return commentMapper.getCommentListByTaskId(taskId);
    }

    @Override
    public ReturnVo addComment(TaskEntity taskEntity, Integer commentType) {

        return null;
    }

}
