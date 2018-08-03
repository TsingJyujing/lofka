#!/bin/bash
ps -ef|grep lofka_tail|grep -v grep|awk '{print $2}'|xargs kill -9
while :
do
	nohup python lofka_tail.py --app_name cvnavi/cvtsp/nginx --file /var/log/nginx/access.log --type nginx --target http://10.10.11.75:9500/ >>lofka_tail.log 2>&1 &
	sleep 1200
	ps -ef|grep lofka_tail|grep -v grep|awk '{print $2}'|xargs kill -9
done
