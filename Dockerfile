FROM python:3.11-slim

WORKDIR /app

COPY ai/ ./ai

RUN pip install --no-cache-dir torch torchvision --index-url https://download.pytorch.org/whl/cpu && pip install pillow

CMD ["python", "ai/predict.py"]