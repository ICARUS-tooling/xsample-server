FROM gradle:7.4.0-jdk8 AS builder
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . /home/gradle/src
RUN rm -f deployment.ini \
	&& gradle build --no-daemon 

FROM payara/server-full:5.2021.10
COPY --from=builder /home/gradle/src/build/libs/xsample-server-*.war ${DEPLOY_DIR}
# The following command is specific to the use if PostgreSQL as DB backend.
# If you wish to use another DB you can skip or modify it.

# Make sure the JDBC driver is available, either in the domain1/lib folder or in glassfish/lib
COPY docker/postgresql-42.2.18.jar ${PAYARA_DIR}/glassfish/domains/domain1/lib