import os
import sys
import pandas as pd

BASE_DIR = os.path.dirname(os.path.realpath(__file__))
sys.path.append(BASE_DIR)
sys.path.append(os.path.join(BASE_DIR, "helpers"))

from sklearn.metrics import roc_auc_score
from helpers.common import load_csv, find_label_column, validate_same_rows

# Load csv
gt = load_csv(sys.argv[1])
pred = load_csv(sys.argv[2])

# Find label columns
gt_col = find_label_column(gt)
pred_col = find_label_column(pred)

if gt_col is None or pred_col is None:
    print(-1)
    exit(0)

# Row count must be same
if not validate_same_rows(gt, pred):
    print(-1)
    exit(0)

try:
    # --- Robust cleaning ---
    # Strip whitespace (ex: "1 ", "0\t", etc.)
    pred[pred_col] = pred[pred_col].astype(str).str.strip()
    gt[gt_col] = gt[gt_col].astype(str).str.strip()

    # Remove empty rows (CSV trailing newline issues)
    pred = pred[pred[pred_col] != ""]
    gt = gt[gt[gt_col] != ""]

    # Must still match row count
    if len(pred) != len(gt):
        print(-1)
        exit(0)

    # Convert to float (handles "1", "1.0", "0 ", etc.)
    y_true = gt[gt_col].astype(float)
    y_score = pred[pred_col].astype(float)

    # --- Calculate AUC ---
    score = roc_auc_score(y_true, y_score)
    print(score)

except Exception as e:
    # Debugging (optional):
    # print("ERROR:", e)
    print(-1)
