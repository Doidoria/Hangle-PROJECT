package com.example.hangle.service;

import com.example.hangle.entity.Dataset;
import com.example.hangle.repository.DatasetRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DatasetService {

    private final DatasetRepository repo;

    public DatasetService(DatasetRepository repo) {
        this.repo = repo;
    }

    // ✅ 모든 데이터 조회 (등록일 최신순)
    public List<Dataset> getAll() {
        return repo.findAllByOrderByCreatedAtDesc();
    }

    // ✅ 좋아요순 조회
    public List<Dataset> getAllSortedByLikes() {
        return repo.findAllByOrderByLikesDesc();
    }

    // ✅ 등록일순 조회
    public List<Dataset> getAllSortedByDate() {
        return repo.findAllByOrderByCreatedAtDesc();
    }

    // ✅ 신규 데이터 저장
    public Dataset save(Dataset dataset) {
        return repo.save(dataset);
    }

    // ✅ 좋아요 +1
    public Dataset increaseLikes(Long id) {
        Dataset dataset = repo.findById(id).orElseThrow();
        dataset.setLikes(dataset.getLikes() + 1);
        return repo.save(dataset);
    }

    // ✅ 좋아요 -1 (취소)
    public Dataset decreaseLikes(Long id) {
        Dataset dataset = repo.findById(id).orElseThrow();
        if (dataset.getLikes() > 0)
            dataset.setLikes(dataset.getLikes() - 1);
        return repo.save(dataset);
    }

    // ✅ 데이터 수정
    public Dataset updateDataset(Long id, Dataset newData) {
        Dataset dataset = repo.findById(id).orElseThrow();
        dataset.setTitle(newData.getTitle());
        dataset.setDescription(newData.getDescription());
        dataset.setAuthor(newData.getAuthor());
        return repo.save(dataset);
    }

    // ✅ 데이터 삭제
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
