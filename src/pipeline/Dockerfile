FROM python:3.10-slim as dagster

ARG DAGSTER_PG_PASSWORD
ARG DAGSTER_PG_HOST
ARG DAGSTER_GRPC_SERVER_HOST
ARG DAGSTER_CURRENT_IMAGE

ENV DAGSTER_HOME=/opt/dagster/dagster_home/
ENV DAGSTER_PG_PASSWORD=$DAGSTER_PG_PASSWORD
ENV DAGSTER_PG_HOST=$DAGSTER_PG_HOST
ENV DAGSTER_GRPC_SERVER_HOST=$DAGSTER_GRPC_SERVER_HOST
ENV DAGSTER_CURRENT_IMAGE=$DAGSTER_CURRENT_IMAGE


RUN mkdir -p $DAGSTER_HOME
WORKDIR $DAGSTER_HOME

COPY dagster.yaml $DAGSTER_HOME
COPY workspace.yaml $DAGSTER_HOME

RUN pip install dagster dagster-postgres dagster-aws

FROM dagster as daemon
COPY daemon-health.py $DAGSTER_HOME
COPY daemon-start.sh $DAGSTER_HOME

RUN pip install Flask
RUN chmod +x ./daemon-start.sh

EXPOSE 4000
EXPOSE 4001

ENTRYPOINT ["./daemon-start.sh"]

FROM dagster as webserver

RUN pip install dagster-webserver dagster-graphql

EXPOSE 3000

ENTRYPOINT ["dagster-webserver", "-h", "0.0.0.0", "-p", "3000", "-w", "workspace.yaml"]
