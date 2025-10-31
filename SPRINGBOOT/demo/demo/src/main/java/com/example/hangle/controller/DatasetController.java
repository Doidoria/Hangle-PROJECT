package com.example.hangle.controller;

import com.example.hangle.entity.Dataset;
import com.example.hangle.service.DatasetService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/datasets")
@CrossOrigin(origins = "http://localhost:8088")
public class DatasetController {

    private final DatasetService service;

    public DatasetController(DatasetService service) {
        this.service = service;
    }

    // ✅ 전체 데이터 조회 (등록일순 기본)
    @GetMapping
    public List<Dataset> getAll() {
        return service.getAll();
    }

    // ✅ 좋아요순 정렬
    @GetMapping("/sort/likes")
    public List<Dataset> getByLikes() {
        return service.getAllSortedByLikes();
    }

    // ✅ 등록일순 정렬
    @GetMapping("/sort/date")
    public List<Dataset> getByDate() {
        return service.getAllSortedByDate();
    }

    // ✅ 신규 데이터 등록
    @PostMapping
    public Dataset add(@RequestBody Dataset dataset) {
        return service.save(dataset);
    }

    // ✅ 좋아요 +1
    @PutMapping("/{id}/like")
    public Dataset increaseLikes(@PathVariable Long id) {
        return service.increaseLikes(id);
    }

    // ✅ 좋아요 -1 (좋아요 취소)
    @DeleteMapping("/{id}/like")
    public Dataset decreaseLikes(@PathVariable Long id) {
        return service.decreaseLikes(id);
    }

    // ✅ 데이터 수정
    @PutMapping("/{id}")
    public Dataset update(@PathVariable Long id, @RequestBody Dataset dataset) {
        return service.updateDataset(id, dataset);
    }

    // ✅ 데이터 삭제
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}


