# Application Setup

### Add Service Account (as sudo account)
- `sudo adduser irby`
- `su - irby`

### Application Image (as service account)
- `unzip /tmp/irby-0.2.0-SNAPSHOT-bin.zip`
- `mkdir store`
- `cd ~/store`
- `openssl genrsa -out server.key`
- `openssl req -new -x509 -days 365 -key server.key -out server.cer -subj "/CN=localhost/OU=irby/"`
- `openssl pkcs12 -export -out server.pkcs12 -inkey server.key -in server.cer`
- `openssl pkcs12 -info -noout -in server.pkcs12`
- `vi ~/irby-0.0.0/conf/app.xml`
  - application ("no-secret")
  - realm ("Irby-Arwo")
  - https11 (keystore, port)

### Add Service (as sudo account)
- `sudo cp /tmp/irby.sh /etc/init.d/irby`
- `sudo chmod +x /etc/init.d/irby`
- `sudo update-rc.d irby defaults`
- `sudo service irby start`
- `sudo service irby status`
- `sudo service irby stop`
- `sudo systemctl enable irby`
- `sudo shutdown -r now`

### Accept on HTTPS port (as sudo account)
- `sudo iptables --list`
- `sudo iptables -t nat --list --line-numbers`
- `sudo iptables -t nat -A PREROUTING -p tcp --dport 443 -j REDIRECT --to-port 8443`
- `sudo iptables -t nat -D PREROUTING 1`
  - (to remove a rule)
- `sudo su`
  - `iptables-save >/etc/iptables/rules.v4`
  - `exit`
- `sudo shutdown -r now`
