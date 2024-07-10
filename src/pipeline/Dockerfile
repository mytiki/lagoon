FROM python:3.10-slim as base

ENV DAGSTER_HOME=/opt/dagster/dagster_home/

RUN mkdir -p $DAGSTER_HOME
WORKDIR $DAGSTER_HOME

COPY dagster.yaml $DAGSTER_HOME
COPY workspace.yaml $DAGSTER_HOME

RUN pip install 'dagster==1.7.12' dagster-postgres dagster-aws

FROM base as daemon
COPY src/daemon/daemon-health.py $DAGSTER_HOME
COPY src/daemon/daemon-start.sh $DAGSTER_HOME

RUN pip install Flask
RUN chmod +x ./daemon-start.sh

EXPOSE 4000
EXPOSE 4001

CMD ["./daemon-start.sh"]

FROM base as dagster

RUN pip install dagster-webserver dagster-graphql

EXPOSE 3000

CMD ["dagster-webserver", "-h", "0.0.0.0", "-p", "3000", "-w", "workspace.yaml"]
