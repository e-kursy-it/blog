#!/bin/bash
# stored in CF as base64 :)
apt-get -y update
apt-get -y install wget ruby-full java-common
cd /home/ubuntu
wget  https://aws-codedeploy-us-east-1.s3.amazonaws.com/latest/install
chmod +x ./install
./install auto
service codedeploy-agent start
wget -nv https://d3pxv6yz143wms.cloudfront.net/11.0.4.11.1/java-11-amazon-corretto-jdk_11.0.4.11-1_amd64.deb
dpkg -i java-11-amazon-corretto-jdk_11.0.4.11-1_amd64.deb