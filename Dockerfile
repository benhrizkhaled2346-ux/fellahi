FROM openjdk:17-slim

# Install Python
RUN apt-get update && apt-get install -y python3 python3-pip

WORKDIR /app

# Copy everything
COPY . .

# Install Python libs (CPU only to avoid heavy build)
RUN pip3 install --no-cache-dir torch torchvision --index-url https://download.pytorch.org/whl/cpu \
    && pip3 install --no-cache-dir pillow

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

CMD ["java", "-jar", "target/ne3mav0.jar"]