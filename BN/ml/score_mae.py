import sys
import pandas as pd
import numpy as np

gt_path = sys.argv[1]
pred_path = sys.argv[2]

gt = pd.read_csv(gt_path)
pred = pd.read_csv(pred_path)

g = gt["label"]
p = pred["label"]

mae = np.abs(g - p).mean()
print(mae)
