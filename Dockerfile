#FROM openjdk:11-jdk-slim as build
#WORKDIR /workspace/app

#COPY mvnw .
#COPY .mvn .mvn
#COPY pom.xml .
#COPY src src
#######################
# RUN apk add --no-cache ffmpeg 

# RUN apt-get update && \
    # apt-get install -y ffmpeg && \
    # apt-get clean && \
    # rm -rf /var/lib/apt/lists/*
# 
#RUN ./mvnw package -DskipTests



#####################
#COPY target/* target/
#RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

#FROM openjdk:11-jre-slim
#VOLUME /tmp
#VOLUME /var/media-server/content

#ARG DEPENDENCY=/workspace/app/target/dependency
#COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
#COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
#COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

#ENTRYPOINT ["java","-cp","app:app/lib/*","com.mediaserver.MediaServerApplication"]



FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace/app

# Install FFmpeg add on 8/8/25
RUN apt-get update && \
    apt-get install -y ffmpeg && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*


COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src


# Copy the entire project including mvnw
COPY . .
COPY mvnw .
RUN chmod +x ./mvnw
# Debug: Print contents of target directory before jar extraction
RUN ./mvnw package -DskipTests  && \
    ls -la target || true


RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM eclipse-temurin:17-jre-jammy
VOLUME /tmp
VOLUME /var/media-server/content

#Install FFmpeg in the runtime image 8/8/25
RUN apt-get update && \
    apt-get install -y ffmpeg && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

ENTRYPOINT ["java","-cp","app:app/lib/*","com.mediaserver.MediaServerApplication"]