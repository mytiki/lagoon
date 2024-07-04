FROM python:3.10-slim as dagster

ENV DAGSTER_HOME=/opt/dagster/dagster_home/

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

CMD ["./daemon-start.sh"]

FROM dagster as webserver

RUN pip install dagster-webserver dagster-graphql

EXPOSE 3000

CMD ["dagster-webserver", "-h", "0.0.0.0", "-p", "3000", "-w", "workspace.yaml"]
