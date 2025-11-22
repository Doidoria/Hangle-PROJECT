import React, { useState } from "react";

async function createFile(url, fileName) {
  const res = await fetch(url);
  const blob = await res.blob();
  return new File([blob], fileName, { type: blob.type || "text/csv" });
}

async function createCompetition(requestObj, trainPath, testPath) {
  const formData = new FormData();

  // JSON request 넣기
  formData.append(
    "request",
    new Blob([JSON.stringify(requestObj)], { type: "application/json" })
  );

  // 파일 fetch → File 객체 변환
  const trainFile = await createFile(trainPath);
  const testFile = await createFile(testPath);

  formData.append("trainFile", trainFile);
  formData.append("testFile", testFile);

  const res = await fetch("http://localhost:8090/api/competitions", {
    method: "POST",
    body: formData,
    credentials: "include"
  });

  return res.ok;
}

export default function AutoCompetitionButton() {
  const [loading, setLoading] = useState(false);

  const createAllCompetitions = async () => {
    if (loading) return;

    setLoading(true);
    alert("자동 대회 생성 시작!");

    const competitions = [
      {
        name: "Accuracy 대회",
        req: {
          title: "감정 분류 - Accuracy 대회",
          description: "텍스트 기반 감정 분류 모델 정확도 평가",
          detail: null,
          status: "UPCOMING",
          startAt: "2025-01-01T00:00",
          endAt: "2025-12-31T23:59",
          evaluationMetric: "ACCURACY",
          prizeTotal: null
        },
        train: "/Competition-datasets/accuracy_train.csv",
        test: "/Competition-datasets/accuracy_test.csv"
      },
      {
        name: "F1 대회",
        req: {
          title: "뉴스 토픽 분류 - F1 대회",
          description: "뉴스 텍스트 기반 분류 모델",
          detail: null,
          status: "UPCOMING",
          startAt: "2025-01-01T00:00",
          endAt: "2025-12-31T23:59",
          evaluationMetric: "F1",
          prizeTotal: null
        },
        train: "/Competition-datasets/f1_train.csv",
        test: "/Competition-datasets/f1_test.csv"
      },
      {
        name: "AUC 대회",
        req: {
          title: "이탈 고객 예측 - AUC 대회",
          description: "고객 이탈 예측 모델 ROC-AUC 평가",
          detail: null,
          status: "UPCOMING",
          startAt: "2025-01-01T00:00",
          endAt: "2025-12-31T23:59",
          evaluationMetric: "AUC",
          prizeTotal: null
        },
        train: "/Competition-datasets/auc_train.csv",
        test: "/Competition-datasets/auc_test.csv"
      },
      {
        name: "RMSE 대회",
        req: {
          title: "주택 가격 예측 - RMSE 대회",
          description: "회귀 RMSE 평가",
          detail: null,
          status: "UPCOMING",
          startAt: "2025-01-01T00:00",
          endAt: "2025-12-31T23:59",
          evaluationMetric: "RMSE",
          prizeTotal: null
        },
        train: "/Competition-datasets/rmse_train.csv",
        test: "/Competition-datasets/rmse_test.csv"
      },
      {
        name: "MAE 대회",
        req: {
          title: "배달 소요시간 예측 - MAE 대회",
          description: "배달 시간 예측 회귀 MAE 평가",
          detail: null,
          status: "UPCOMING",
          startAt: "2025-01-01T00:00",
          endAt: "2025-12-31T23:59",
          evaluationMetric: "MAE",
          prizeTotal: null
        },
        train: "/Competition-datasets/mae_train.csv",
        test: "/Competition-datasets/mae_test.csv"
      }
    ];

    let successCount = 0;
    let failCount = 0;
    let failList = [];

    for (const comp of competitions) {
      try {
        const ok = await createCompetition(comp.req, comp.train, comp.test);
        console.log(`대회 생성 성공: ${comp.name}`);
        successCount++;
      } catch (err) {
        console.error(`❌ 대회 생성 실패: ${comp.name}`, err);
        failCount++;
        failList.push(comp.name);
      }
    }

    if (failCount === 0) {
      alert(`🎉 모든 대회 자동 생성 성공! (${successCount}개)`);
    } else {
      alert(
        `⚠ 일부 대회 생성 실패!\n성공: ${successCount}, 실패: ${failCount}\n실패 목록:\n• ${failList.join(
          "\n• "
        )}`
      );
    }

    setLoading(false);
  };

  return (
    <button type="button" onClick={createAllCompetitions} disabled={loading}
      style={{ padding: "10px 20px", background: loading ? "#bd3434ff" : "#3a22beff",
        color: "white", borderRadius: "8px",
        cursor: loading ? "not-allowed" : "pointer", transition: "0.2s"
      }}>
      {loading ? "생성 중..." : "자동 대회 생성"}
    </button>
  );
}
