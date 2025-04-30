FROM maven:3.6.3-jdk-8 AS builder
WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests


FROM tomcat:8.5.86-jre8

COPY --from=builder /app/businessServer/target/BusinessServer.war /usr/local/tomcat/webapps/ROOT.war

RUN cd /usr/local/tomcat/conf && \
    grep -l "8080" server.xml && \
    sed -i 's/port="8080"/port="8088"/g' server.xml && \
    sed -i 's/redirectPort="8443"/redirectPort="9443"/g' server.xml && \
    cat server.xml | grep "port=" && \
    echo "Tomcat will use port 8088"

EXPOSE 8088

ENV CATALINA_OPTS="-Dport.http.nonssl=8088"

CMD ["catalina.sh", "run"]  