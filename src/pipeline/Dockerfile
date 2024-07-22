FROM python:3.10-slim AS base

ENV DAGSTER_HOME=/opt/dagster/dagster_home/


RUN mkdir -p $DAGSTER_HOME
WORKDIR $DAGSTER_HOME

COPY dagster.yaml $DAGSTER_HOME
COPY workspace.yaml $DAGSTER_HOME
COPY requirements.txt $DAGSTER_HOME/requirements-base.txt

RUN pip install -r requirements-base.txt

FROM base AS daemon

COPY src/daemon/daemon-health.py $DAGSTER_HOME
COPY src/daemon/daemon-start.sh $DAGSTER_HOME
COPY src/daemon/requirements.txt $DAGSTER_HOME/requirements-daemon.txt

RUN pip install -r requirements-daemon.txt
RUN chmod +x ./daemon-start.sh

EXPOSE 4000
EXPOSE 4001

CMD ["./daemon-start.sh"]

FROM base AS dagster

COPY src/dagster/requirements.txt $DAGSTER_HOME/requirements-dagster.txt

RUN pip install -r requirements-dagster.txt

EXPOSE 3000

CMD ["dagster-webserver", "-h", "0.0.0.0", "-p", "3000", "-w", "workspace.yaml"]
