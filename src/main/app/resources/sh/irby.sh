#!/bin/sh

### BEGIN INIT INFO
# Provides:          irby
# Required-Start:    $all
# Required-Stop:
# Default-Start:     2 3 4 5
# Default-Stop:
# Short-Description: Irby Application
### END INIT INFO

IRBY_VERSION=0.2.0-SNAPSHOT
IRBY_HOME=/home/irby/irby-$IRBY_VERSION

# https://github.com/fhd/init-script-template/blob/master/template

start()
{
echo "Starting..."
daemon -nirby -uirby -D$IRBY_HOME -r -A1 -L10 -- /usr/bin/java -Djava.util.logging.config.file=$IRBY_HOME/conf/logging.properties -jar $IRBY_HOME/irby-$IRBY_VERSION.jar
}

stop()
{
echo "Stopping..."
daemon -nirby -uirby --stop
}

case "$1" in
  start)
        start
        ;;
  stop)
        stop
        ;;
  *)
        echo "Usage: irby {start|stop}"
        exit 1
esac

exit 0
