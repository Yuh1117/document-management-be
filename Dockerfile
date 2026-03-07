FROM maven:3.9-eclipse-temurin-21 AS build

RUN apt-get update && apt-get install -y \
    tesseract-ocr \
    tesseract-ocr-vie \
    tesseract-ocr-eng \
    && rm -rf /var/lib/apt/lists/* \

ENV TESSERACT_PATH=/usr/share/tesseract-ocr/4.00/tessdata

WORKDIR /app

COPY pom.xml ./

COPY src/main/resources/vncorenlp /app/src/main/resources/vncorenlp

RUN mvn dependency:resolve

CMD ["mvn", "spring-boot:run"]
