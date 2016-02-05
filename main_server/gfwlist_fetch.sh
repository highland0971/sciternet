#!/bin/sh

GFW_MID_LIST='gfwlist.mid'
GFW_LIST='gfwlist.list'
rm $GFW_LIST $GFW_MID_LIST
touch $GFW_LIST
touch $GFW_MID_LIST

wget -O /tmp/page http://www.hikinggfw.org/blacklist/?page=1
max_page=0
for num in $( egrep '<li><a href="\?page=' /tmp/page |egrep -o [0-9]*) ;do
        if [ $num -gt $max_page ];then
                max_page=$num;
        fi
done

echo max page from http://www.hikinggfw.org/blacklist is $max_page

for num in $(seq $max_page); do
        egrep -o '[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+\.?' /tmp/page >>$GFW_MID_LIST
        echo fetching page $num
        wget -qO /tmp/page http://www.hikinggfw.org/blacklist/?page=$num
done

for candidate in $(python gfwlist_fetch.py $GFW_MID_LIST); do
	nslookup $candidate >/dev/null && echo server=/$candidate/8.8.4.4 >> $GFW_LIST && echo domain $candidate added.
done

