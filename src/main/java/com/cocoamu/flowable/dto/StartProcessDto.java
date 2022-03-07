package com.cocoamu.flowable.dto;

import lombok.Data;

import java.util.List;

@Data
public class StartProcessDto {
    private String processKey;
    List<UpdateTaskDto> expressList;

}
