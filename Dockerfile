FROM python:3.11-slim

WORKDIR /app

COPY ai/ ./ai

RUN pip install torch torchvision pillow

CMD ["python", "ai/predict.py"]