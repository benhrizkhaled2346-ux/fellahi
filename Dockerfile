FROM python:3.11-slim

WORKDIR /app

COPY ai/ ./ai
COPY requirements.txt .

RUN pip install --no-cache-dir -r requirements.txt

CMD ["python", "ai/predict.py"]