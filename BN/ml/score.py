import sys
import pandas as pd

"""
사용법:
python ml/score.py <정답 파일 경로> <제출 파일 경로>
"""

if len(sys.argv) < 3:
    print("-1")
    exit(0)

gt_path = sys.argv[1]
pred_path = sys.argv[2]

try:
    gt = pd.read_csv(gt_path)
    pred = pd.read_csv(pred_path)
except Exception as e:
    print("-1")
    exit(0)

# label 컬럼 존재 확인
if "label" not in gt.columns or "label" not in pred.columns:
    print("-1")
    exit(0)

# 행 개수 mismatch 체크
if len(gt) != len(pred):
    print("-1")
    exit(0)

# Accuracy 계산
correct = (gt["label"] == pred["label"]).sum()
total = len(gt)
score = correct / total

print(score)
