#!/usr/bin/env bash

LOCAL_IPV4=`curl http://169.254.169.254/latest/meta-data/local-ipv4`

sudo useradd nettyapp

sudo chown nettyapp:nettyapp /opt/nettyapp/run.sh

sudo chmod a+x /opt/nettyapp/run.sh

read -r -d '' SERVICE << SERVICE
[Unit]
Description=Netty Http Server Example
[Service]
Restart=always
StandardOutput=syslog
StandardError=syslog
SyslogIdentifier=nettyapp
User=nettyapp
Group=nettyapp
ExecStart=/opt/nettyapp/run.sh $LOCAL_IPV4 /home/ubuntu
[Install]
WantedBy=multi-user.target
SERVICE

echo "$SERVICE" | sudo tee /etc/systemd/system/nettyapp.service
sudo systemctl enable nettyapp