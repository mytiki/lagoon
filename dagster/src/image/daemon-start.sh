#!/bin/bash
python3 daemon-health.py &
dagster-daemon run -w workspace.yaml &
wait
