package com.example.demo.domain.competition.service;

import com.example.demo.domain.competition.entity.Competition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreService {

    @Value("${ml.script-dir}")
    private String scriptDir;

    public double runScore(Competition competition, String answerPath, String submitPath) {

        String script;
        // 1) 커스텀 스크립트가 있으면 그것을 실행
        if (competition.getCustomScorePath() != null) {
            script = competition.getCustomScorePath();
        }
        // 2) 평가 지표별 스크립트 선택 (대소문자 무시 처리)
        else {
            String metric = competition.getEvaluationMetric() == null ? "ACCURACY" : competition.getEvaluationMetric().toUpperCase();
            script = switch (metric) {
                case "F1" -> "score_f1.py";
                case "AUC" -> "score_auc.py";
                case "RMSE" -> "score_rmse.py";
                case "MAE" -> "score_mae.py";
                default -> "score_accuracy.py";
            };
        }
        String scriptPath = Paths.get(scriptDir, script).toAbsolutePath().toString();

        // 1) Python 실행 → 원본 점수(rawScore)
        double rawScore = runPython(scriptPath, answerPath, submitPath);

        // Python -1 반환 = 채점 실패
        if (rawScore < 0) return -1;

        // 2) 100점 환산 계산
        double finalScore;
        String metric = competition.getEvaluationMetric() == null ? "ACCURACY" : competition.getEvaluationMetric().toUpperCase();

        switch (metric) {
            case "MAE":
            case "RMSE":
                // 로그 기반 역수 변환, 오차(rawScore)가 0이면 100점
                // 오차가 커질수록 점수는 완만하게 떨어짐 (집값처럼 단위가 커도 점수가 0이 되지 않음)
                finalScore = 100.0 / (1.0 + Math.log1p(rawScore));
                break;

            default:
                // F1, AUC, ACC → rawScore가 0~1 범위 → 100점 환산
                // 혹시 모를 1.0 초과 값 방지를 위해 Math.min 적용
                finalScore = Math.min(100.0, rawScore * 100.0);
                break;
        }

        log.info("RAW SCORE = {}", rawScore);
        log.info("FINAL NORMALIZED SCORE (100점 환산) = {}", finalScore);

        return finalScore;
    }

    private double runPython(String script, String answerPath, String submitPath) {
        try {
            String scriptPath = Paths.get(script).toAbsolutePath().toString();
            log.error("SCRIPT PATH = {}", scriptPath);

            ProcessBuilder pb = new ProcessBuilder(
                    "python3", //배포 시 오류나면 python3, py
                    script,
                    answerPath,
                    submitPath
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            StringBuilder output = new StringBuilder();
            String line;
            String lastLine = null;

            // Python 출력 전체 읽기 + 마지막 줄 저장
            while ((line = br.readLine()) != null) {
                output.append(line).append("\n");
                lastLine = line; // 마지막 줄이 보통 점수
            }

            int exit = process.waitFor();

            if (exit != 0) {
                log.error("Python 채점 실패(exit={}):\n{}", exit, output);
                return -1;
            }
            if (lastLine == null) {
                log.error("Python 출력이 없습니다.");
                return -1;
            }
            log.info("Python 출력(last line) = {}", lastLine);

            // 마지막 줄의 공백 제거 후 파싱
            return Double.parseDouble(lastLine.trim());

        } catch (Exception e) {
            log.error("채점 중 예외 발생: {}", e.getMessage(), e);
            return -1;
        }
    }
}
