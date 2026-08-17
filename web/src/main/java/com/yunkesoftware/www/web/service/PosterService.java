package com.yunkesoftware.www.web.service;

import com.yunkesoftware.www.web.dto.PosterGenerateRequest;

import java.util.List;
import java.util.Map;

public interface PosterService {
    List<Map<String, Object>> listPublished();
    Map<String, Object> generate(String userId, PosterGenerateRequest request);
    byte[] download(String userId, String generationId);
}
