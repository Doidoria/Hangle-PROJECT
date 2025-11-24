FROM ollama/ollama:latest

# start.sh 복사 및 Windows CRLF -> Unix LF 변환
COPY start.sh /start.sh
RUN sed -i 's/\r$//' /start.sh && chmod +x /start.sh

ENV OLLAMA_ORIGINS=*
ENV OLLAMA_AUTO_PULL_MODEL=gemma2:2b

EXPOSE 11434

ENTRYPOINT ["/start.sh"]

