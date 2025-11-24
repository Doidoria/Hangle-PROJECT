import sys
import os
import numpy as np

BASE_DIR = os.path.dirname(os.path.realpath(__file__))
sys.path.append(BASE_DIR)
sys.path.append(os.path.join(BASE_DIR, "helpers"))

from helpers.common import load_csv, find_label_column, validate_same_rows

gt = load_csv(sys.argv[1])
pred = load_csv(sys.argv[2])

gt_col = find_label_column(gt)
pred_col = find_label_column(pred)

if gt_col is None or pred_col is None:
    print(-1)
    exit(0)

if not validate_same_rows(gt, pred):
    print(-1)
    exit(0)

mae = np.abs(gt[gt_col] - pred[pred_col]).mean()
print(mae)
