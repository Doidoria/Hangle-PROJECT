package com.example.demo.domain.competition.service;

import com.example.demo.domain.competition.entity.Competition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreService {

    // 3. 반환 타입을 double -> CompletableFuture<Double>로 변경
    public CompletableFuture<Double> runScore(Competition competition, String answerPath, String submitPath) {

        if (competition.getCustomScorePath() != null) {
            return runPython(competition.getCustomScorePath(), answerPath, submitPath);
        }

        String metric = competition.getEvaluationMetric();

        String script = switch (metric) {
            case "F1" -> "ml/score_f1.py";
            case "RMSE" -> "ml/score_rmse.py";
            case "MAE" -> "ml/score_mae.py";
            default     -> "ml/score_accuracy.py";
        };

        return runPython(script, answerPath, submitPath);
    }


    // 4. @Async 어노테이션 추가
    @Async
    private CompletableFuture<Double> runPython(String script, String answerPath, String submitPath) {
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
            int exit = process.waitFor(); // 이 부분은 백그라운드 스레드에서 대기

            if (exit != 0 || line == null) {
                log.error("채점 스크립트 실행 오류 또는 결과 없음. Exit Code: {}", exit);
                // 5. 실패 시 -1.0을 완료된 CompletableFuture에 담아 반환
                return CompletableFuture.completedFuture(-1.0);
            }

            double score = Double.parseDouble(line);
            // 6. 성공 시 결과를 완료된 CompletableFuture에 담아 반환
            return CompletableFuture.completedFuture(score);

        } catch (Exception e) {
            log.error("파이썬 프로세스 실행 중 예외 발생: {}", e.getMessage(), e);
            // 7. 예외 발생 시 -1.0을 완료된 CompletableFuture에 담아 반환
            return CompletableFuture.completedFuture(-1.0);
        }
    }
}