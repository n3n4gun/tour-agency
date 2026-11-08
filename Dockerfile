FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /tour-agency

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn -B -ntp -DskipTests package \
    && cp target/tour-agency-*.jar application.jar \
    && java -Djarmode=tools -jar application.jar \
    && extract --layers --destination extracted

FROM eclipse-temurin:25-jre-jammy
WORKDIR /tour-agency

RUN groupadd --gid 10001 touruser \
    && useradd --uid 10001 --gid touruser \
       --no-create-home --shell /usr/sbin/nologin touruser

COPY --from=build --chown=touruser:touruser /tour-agency/extracted/dependencies/ ./
COPY --from=build --chown=touruser:touruser /tour-agency/extracted/spring-boot-loader/ ./
COPY --from=build --chown=touruser:touruser /tour-agency/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=touruser:touruser /tour-agency/extracted/application/ ./

USER touruser

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "application.jar"]