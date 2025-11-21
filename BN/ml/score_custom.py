import sys
import pandas as pd

gt_path = sys.argv[1]
pred_path = sys.argv[2]

# 원하는 방식으로 자유롭게 구현
gt = pd.read_csv(gt_path)
pred = pd.read_csv(pred_path)

# 일단 accuracy 방식 예시
correct = (gt["label"] == pred["label"]).sum()
score = correct / len(gt)

print(score)
