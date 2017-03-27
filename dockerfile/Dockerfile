FROM		openjdk:8-jre
MAINTAINER 	JAkub Scholz "www@scholzj.com"

RUN addgroup --system dave && adduser --system --home /home/dave --group dave
COPY ./dave-1.0-SNAPSHOT /home/dave

ENV DAVE_VERSION 1.0-SNAPSHOT

ENV JAVA_OPTS -Xmx=512m

RUN chown -R dave:dave /home/dave
WORKDIR /home/dave

# Add entrypoint
COPY ./docker-entrypoint.sh /
ENTRYPOINT ["/docker-entrypoint.sh"]

USER dave

# Expose port and run
EXPOSE 8080
CMD ["./bin/start_dave.sh"]
