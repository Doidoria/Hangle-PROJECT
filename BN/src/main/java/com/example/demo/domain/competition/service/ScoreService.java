package com.example.demo.domain.competition.service;

import com.example.demo.domain.competition.entity.Competition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public double runScore(Competition competition, String answerPath, String submitPath) {

        String script;

        // 1) 커스텀 스크립트가 있으면 그것을 실행
        if (competition.getCustomScorePath() != null) {
            script = competition.getCustomScorePath();
        }
        // 2) 평가 지표별 스크립트 선택
        else {
            script = switch (competition.getEvaluationMetric()) {
                case "F1" -> "ml/score_f1.py";
                case "AUC" -> "ml/score_auc.py";
                case "RMSE" -> "ml/score_rmse.py";
                case "MAE" -> "ml/score_mae.py";
                default -> "ml/score_accuracy.py";
            };
        }
        String absoluteScriptPath = Paths.get(script).toAbsolutePath().toString();

        return runPython(absoluteScriptPath, answerPath, submitPath);
    }

    private double runPython(String script, String answerPath, String submitPath) {
        try {
            String scriptPath = Paths.get(script).toAbsolutePath().toString();
            log.error("SCRIPT PATH = {}", scriptPath);

            ProcessBuilder pb = new ProcessBuilder(
                    "python",
                    scriptPath,
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
                lastLine = line;
            }

            int exit = process.waitFor();

            if (exit != 0) {
                log.error("🔥 Python 채점 실패(exit={}):\n{}", exit, output);
                return -1;
            }

            if (lastLine == null) {
                log.error("🔥 Python 출력이 없습니다.");
                return -1;
            }

            log.info("🔥 Python 출력(last line) = {}", lastLine);

            return Double.parseDouble(lastLine.trim());

        } catch (Exception e) {
            log.error("🔥 채점 중 예외 발생: {}", e.getMessage(), e);
            return -1;
        }
    }
}
