FROM gradle:7.4.0-jdk8 AS builder
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . /home/gradle/src
RUN rm -f deployment.ini \
	&& gradle build --no-daemon 

FROM payara/server-full:5.2021.10
COPY --from=builder /home/gradle/src/build/libs/xsample-server-*.war ${DEPLOY_DIR}