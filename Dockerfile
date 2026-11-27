FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q clean package -DskipTests

# YOLOv8 fracture model, an asset of the GitHub Release model-v1 (see ml/README.md).
# The checksum stops the build if the downloaded file is not the expected model.
# Build with --build-arg MODEL_URL= to leave it out (the app then uses the placeholder model).
FROM maven:3.9-eclipse-temurin-21 AS model
ARG MODEL_URL=https://github.com/teq31/mobile-bone-fracture-detection/releases/download/model-v1/yolov8-fracture.onnx
ARG MODEL_SHA256=ba1b587fb3e47e79884915c11079610d7f95c230428a6f77c9038dad69180df7
RUN mkdir -p /model && if [ -n "$MODEL_URL" ]; then \
        curl -fsSL --retry 3 -o /model/yolov8-fracture.onnx "$MODEL_URL" && \
        echo "$MODEL_SHA256  /model/yolov8-fracture.onnx" | sha256sum -c -; \
    fi

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY --from=model /model /opt/model
ENV YOLO_MODEL=/opt/model/yolov8-fracture.onnx
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
