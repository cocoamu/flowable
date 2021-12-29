package com.cocoamu.flowable.controller;

import com.cocoamu.flowable.dto.Comment;
import com.cocoamu.flowable.service.MyCommentService;
import com.cocoamu.flowable.vo.ReturnVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/comment")
public class CommentController {
    @Autowired
    MyCommentService myCommentService;

    /**
     * 根据流程实例id获取评论列表
     * @param processId 流程实例id
     * @return
     */
    @PostMapping(value = "/getCommentListByProcessId")
    public ReturnVo getCommentListByProcessId(String processId) {
        List<Comment> commentList = myCommentService.getCommentListByProcessId(processId);
        return ReturnVo.sucess(commentList);
    }

    /**
     * 根据任务id获取评论列表
     * @param taskId 任务id
     * @return
     */
    @PostMapping(value = "/getCommentListByTaskId")
    public ReturnVo getCommentListByTaskId(String taskId) {
        List<Comment> commentList = myCommentService.getCommentListByTaskId(taskId);
        return ReturnVo.sucess(commentList);
    }

    /**
     * 新增评论
     * @param comment 评论实体
     * @return
     */
    @RequestMapping(value = "/add")
    public ReturnVo getBpmnXmlByPid(@RequestBody Comment comment)  {
        return myCommentService.addComment(comment);
    }
}
