#!/bin/sh

# https://github.com/fhd/init-script-template/blob/master/template

start()
{
echo "Starting..."
daemon -nirby -uirby -D/home/irby -r -A1 -L10 -- /usr/bin/java -Djava.util.logging.config.file=logging.properties -jar irby.jar
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
