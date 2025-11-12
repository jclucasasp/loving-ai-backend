#!/bin/sh

# Create log dir
mkdir -p /logs

# Rotate yesterday's log
if [ -f /logs/app.log ]; then
  mv /logs/app.log /logs/$(date -d "yesterday" +%Y-%m-%d).txt
fi

# Start the app with logging
exec /cnb/process/web 2>&1 | tee /logs/app.log
