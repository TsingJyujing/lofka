FROM openjdk:11-stretch
MAINTAINER TsingJyujing <tsingjyujing@163.com>
COPY lofka-server/target/lofka-server-*-SNAPSHOT.jar /app/lofka-server.jar
COPY conf/docker-lite/* /app/conf/
WORKDIR /app
EXPOSE 9500
CMD ["java","-jar","lofka-server.jar"]
