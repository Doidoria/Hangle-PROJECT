#!/bin/sh
set -e

MODEL_TO_PULL="${OLLAMA_AUTO_PULL_MODEL:-gemma2:2b}"

echo "Starting ollama serve in background..."
ollama serve &
SERVER_PID=$!

# 서버가 준비될 때까지 대기
sleep 5

echo "Attempting to pull model: ${MODEL_TO_PULL}"
if ! ollama pull "${MODEL_TO_PULL}"; then
	echo "Failed to pull ${MODEL_TO_PULL}. You can pull it manually later."
fi

wait ${SERVER_PID}
