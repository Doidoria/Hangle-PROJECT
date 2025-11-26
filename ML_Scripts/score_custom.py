import os
import sys
BASE_DIR = os.path.dirname(os.path.realpath(__file__))
sys.path.append(BASE_DIR)
sys.path.append(os.path.join(BASE_DIR, "helpers"))

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
