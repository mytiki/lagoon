FROM python:3.10-slim

ENV DAGSTER_HOME=/opt/dagster/dagster_home/

RUN mkdir -p $DAGSTER_HOME
WORKDIR $DAGSTER_HOME

COPY . .
RUN pip install -r requirements.txt

EXPOSE 4000

ENTRYPOINT ["dagster", "api", "grpc", "-h", "0.0.0.0", "-p", "4000", "-f", "defs.py"]
