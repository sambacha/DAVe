FROM		openjdk:8-jre
MAINTAINER 	JAkub Scholz "www@scholzj.com"

RUN adduser --system --home /home/dave dave
COPY ./dave-1.0-SNAPSHOT /home/dave

RUN chown -R dave /home/dave
WORKDIR /home/dave

# Add entrypoint
COPY ./docker-entrypoint.sh /
ENTRYPOINT ["/docker-entrypoint.sh"]

USER dave

# Expose port and run
EXPOSE 8080
CMD ["./bin/start_dave.sh"]