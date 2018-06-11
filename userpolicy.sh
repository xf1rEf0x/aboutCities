#!/bin/bash
echo "Please,enter user name"
read name
useradd $name
# ask user to change passwd after first login
passwd -e $name
# ask temporary usr passwd
passwd $name
echo "user $name successfuly added"
# set uer expire time
chage -m 0 -M 90 -I 30 -W 14 $name
# show information about user's passwd
chage -l $name

chgrp_iptable ()
{

    chgrp $name /sbin/xtables-multi
    echo "group for IPTABLES binary succesfully changed to $name"
    chgrp $name /var/log/syslog
    echo "group for /var/log/syslog file succesfully changed to $name"

}

get_access ()
{

    chmod g+s /sbin/xtables-multi
    echo "SetGID bit added to /sbin/xtables-multi "
    chmod u+s /sbin/xtables-multi
    echo "SetUID bit added to /sbin/xtables-multi "
    chmod u+s /var/log/syslog
    echo "SetUID bit added to /var/log/syslog "
    chmod g+s /var/log/syslog
    echo "SetGID bit added to /var/log/syslog "

}

chgrp_iptable
get_access

echo "Script was succesfully finished"
