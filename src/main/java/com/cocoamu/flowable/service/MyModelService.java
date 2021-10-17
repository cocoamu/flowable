package com.cocoamu.flowable.service;

import com.fasterxml.jackson.databind.JsonNode;

public interface MyModelService {
    String saveModelJSON(JsonNode modelNode);
}
