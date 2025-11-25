import pandas as pd

def load_csv(path):
    return pd.read_csv(path)

def find_label_column(df):
    """
    label 컬럼 자동 탐지:
    - label
    - target
    - y
    - answer
    - gt
    등 다양한 이름 자동 지원
    """
    candidates = ["label", "target", "y", "answer", "gt", "class", "output"]

    lower_cols = [c.lower() for c in df.columns]

    for cand in candidates:
        if cand in lower_cols:
            return df.columns[lower_cols.index(cand)]

    return None  # 못 찾으면 None

def validate_same_rows(df1, df2):
    return len(df1) == len(df2)
