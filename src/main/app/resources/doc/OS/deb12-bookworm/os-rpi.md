# OS Setup

### Image to SD Card
- Raspberry Pi Imager (v1.6 or newer)
- [OS images](https://www.raspberrypi.com/software/operating-systems/)
- "Raspberry Pi OS (64-bit) / Raspberry Pi OS with desktop"

### SD Card to Pi
- Power On (auto resize of volume; auto reboot)

### Welcome Wizard
- locale
- account
- wifi
- browser 
- system updates
- restart

### Post Install
- Preferences / Appearance Settings
- Preferences / Raspberry Pi Configuration
  - Hostname
  - Auto login
  - SSH
  - Timezone
- restart
- ssh-copy-id

### Application Setup
- `sudo apt-get install daemon`
  - (run as service)
- `sudo apt-get install iptables iptables-persistent`
  - (listen on 443)
- `sudo apt-get install uuid-runtime`
  - (`uuidgen`)
- `sudo apt-get install default-jdk`
