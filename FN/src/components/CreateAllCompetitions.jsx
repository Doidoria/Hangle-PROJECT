import React, { useState } from "react";

// 날짜를 'YYYY-MM-DDTHH:mm' 형식의 문자열로 변환하는 함수
function getFormattedDate(date) {
  const pad = (n) => String(n).padStart(2, '0');
  const year = date.getFullYear();
  const month = pad(date.getMonth() + 1);
  const day = pad(date.getDate());
  
  // 백엔드 LocalDateTime 포맷에 맞추기 위해 시간(00:00:00)을 강제로 붙임
  return `${year}-${month}-${day}T00:00:00`;
}

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

  const res = await fetch(`${process.env.REACT_APP_API_BASE_URL}/api/competitions`, {
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
    if (!window.confirm("자동 대회 생성을 시작하시겠습니까?")) {
        return;
    }

    // 시간 설정 로직 간소화
    const now = new Date();
    
    // 시작일: 어제 날짜 (시간 무관, 날짜만 사용됨)
    const startDate = new Date(now);
    startDate.setDate(startDate.getDate() - 1); 

    // 종료일: 7일 후 날짜
    const endDate = new Date(now);
    endDate.setDate(endDate.getDate() + 7);

    // 함수가 이제 'YYYY-MM-DD' 문자열만 반환함
    const startStr = getFormattedDate(startDate);
    const endStr = getFormattedDate(endDate);

    const competitions = [
      {
        name: "Accuracy 대회",
        req: {
          title: "감정 분류 - Accuracy 대회",
          description: "텍스트 기반 감정 분류 모델 정확도 평가",
          detail: null,
          status: "OPEN",
          startAt: startStr,
          endAt: endStr,
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
          status: "OPEN",
          startAt: startStr,
          endAt: endStr,
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
          status: "OPEN",
          startAt: startStr,
          endAt: endStr,
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
          status: "OPEN",
          startAt: startStr,
          endAt: endStr,
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
          status: "OPEN",
          startAt: startStr,
          endAt: endStr,
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
        console.log(`대회 생성 성공[O]: ${comp.name}`);
        successCount++;
      } catch (err) {
        console.error(`대회 생성 실패[X]: ${comp.name}`, err);
        failCount++;
        failList.push(comp.name);
      }
    }

    if (failCount === 0) {
      alert(`🎉 모든 대회 자동 생성 성공! (${successCount}개)`);
      window.location.href = '/competitions/list';
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
