# Configure Linux Service

> From machine *LOCALHOST*; setup application *irby* on machine *IRBYSERVER*.

* add user for process
```shell
root@irbyserver: adduser irby
```

* copy binary to target machine
```shell
greyp9@localhost: scp ~/irby/target/app/artifact/irby.bXXX.zip irby@irbyserver:~
```

* acquire ownership of binary
```shell
root@irbyserver: chown irby:irby ~/irby.bXXX.zip
irby@irbyserver: unzip ~/irby.bXXX.zip
```

---

> Now that the application is on target machine, we configure a startup script to manage the application.

* put server script
```shell
greyp9@localhost: scp ~/irby/src/main/app/resources/sh/irby.sh root@irbyserver:/etc/init.d/irby
root@irbyserver: chmod +x /etc/init.d/irby
```

* run script at startup
```shell
root@irbyserver: apt-get install daemon
root@irbyserver: update-rc.d irby defaults
```

* activate script configuration
```shell
root@irbyserver: systemctl daemon-reload
```

* exercise script
```shell
root@irbyserver: service irby start
root@irbyserver: service irby stop
root@irbyserver: shutdown -r now
root@irbyserver: ps -ef | grep java
```

---

> Now that the application is running, we can test access.

* request application home page
```shell
https://irbyserver:8443
```

* use generated credentials (from application log) to access application
