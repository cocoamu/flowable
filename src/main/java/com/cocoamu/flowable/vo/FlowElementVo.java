package com.cocoamu.flowable.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlowElementVo {
    @JsonProperty("element_id")
    private String elementId;
    @JsonProperty("element_name")
    private String elementName;
}
