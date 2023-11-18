# Raspberry Pi (Port Forward)

##### Enumerate interfaces
- `ip addr`
- `ip link show`  (alternate view)
- `/sbin/ifconfig`  (alternate view)
- `netstat -i`  (alternate view)

##### List all rules
- `sudo iptables --list`
- `sudo iptables --list-rules`  (alternate view)
- `sudo iptables-save`  (alternate view)

##### List all rules in "NAT" table
- `sudo iptables -t nat --list --line-numbers`

##### Add rule in "NAT" table
- `sudo iptables -t nat -A PREROUTING -p tcp --dport 443 -j REDIRECT --to-port 8443`

##### Remove all rules from "NAT" table
- `sudo iptables --flush`

##### Remove rule from "NAT" table
- `sudo iptables -t nat -D PREROUTING 1`

##### Environment
- rpi (Debian 11.6)

##### Reference
- [Local Port Redirection (debian.org)](https://wiki.debian.org/Firewalls-local-port-redirection)
