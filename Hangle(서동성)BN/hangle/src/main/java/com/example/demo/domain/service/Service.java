package com.example.demo.domain.service;

import org.springframework.stereotype.Service;
import com.example.demo.domain.entity.Dataset;
import com.example.demo.domain.repository.DatasetRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class HomeService {

    private final DatasetRepository datasetRepository;

    public HomeService(DatasetRepository datasetRepository) {
        this.datasetRepository = datasetRepository;
    }

    public List<Dataset> getTrendingDatasets() {
        return datasetRepository.findTop5ByOrderByViewCountDesc();
    }
}
