# Additional

### Resources to IRBYMACHINE
- `scp irby/target/irby-$IRBYVERSION-bin.zip user@irbymachine:/tmp/`
- `scp irby/src/main/app/resources/sh/irby.sh user@irbymachine:/tmp/`

### Network Context
- `ip addr`
- `sudo nft list tables`

### OS Context
- `uptime`
- `sudo shutdown -r now`
- `sudo daemon --list`

### Application Context
- `ps -f -u irby`


### Reference
- [Problem with my iptables configuration on reboot](https://askubuntu.com/questions/1452706/problem-with-my-iptables-configuration-on-reboot)
- [nftables](https://wiki.debian.org/nftables)
- [How to use Nftables to route port 80 to 8080](https://bbs.archlinux.org/viewtopic.php?id=225429)
- [How do I start a service on boot in debian?](https://stackoverflow.com/questions/37349253/how-do-i-start-a-service-on-boot-in-debian)
- [Moving from iptables to nftables](https://wiki.nftables.org/wiki-nftables/index.php/Moving_from_iptables_to_nftables)
- [Quick reference-nftables in 10 minutes](https://wiki.nftables.org/wiki-nftables/index.php/Quick_reference-nftables_in_10_minutes)

### nftables snippet
```
apt-get install nftables

table ip nat {
        chain prerouting {
                type nat hook prerouting priority 0; policy accept;
                tcp dport 80 redirect to 8080
        }

        chain postrouting {
                type nat hook postrouting priority 0; policy accept;
        }
}
```
