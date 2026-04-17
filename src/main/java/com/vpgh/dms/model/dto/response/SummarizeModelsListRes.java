package com.vpgh.dms.model.dto.response;

import java.util.List;

public class SummarizeModelsListRes {
    private List<SummarizeModelInfoRes> models;

    public SummarizeModelsListRes() {
    }

    public SummarizeModelsListRes(List<SummarizeModelInfoRes> models) {
        this.models = models;
    }

    public List<SummarizeModelInfoRes> getModels() {
        return models;
    }

    public void setModels(List<SummarizeModelInfoRes> models) {
        this.models = models;
    }
}
