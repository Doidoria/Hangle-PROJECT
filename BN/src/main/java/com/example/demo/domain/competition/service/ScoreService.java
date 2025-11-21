package com.example.demo.domain.competition.service;

import com.example.demo.domain.competition.entity.Competition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreService {

    public double runScore(Competition competition, String answerPath, String submitPath) {

        // 1) 커스텀 score.py 있는지 확인
        if (competition.getCustomScorePath() != null) {
            return runPython(competition.getCustomScorePath(), answerPath, submitPath);
        }

        // 2) 기본 metric-based 점수 스크립트
        String metric = competition.getEvaluationMetric();

        String script = switch (metric) {
            case "F1" -> "ml/score_f1.py";
            case "RMSE" -> "ml/score_rmse.py";
            case "MAE" -> "ml/score_mae.py";
            default     -> "ml/score_accuracy.py";
        };

        return runPython(script, answerPath, submitPath);
    }


    private double runPython(String script, String answerPath, String submitPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "python",
                    script,
                    answerPath,
                    submitPath
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String line = br.readLine();
            int exit = process.waitFor();

            if (exit != 0) return -1;
            if (line == null) return -1;

            return Double.parseDouble(line);

        } catch (Exception e) {
            return -1; // 채점 실패
        }
    }
}


