#!/bin/bash
# /etc/init.d/xenPoolHostShutdownManager

### BEGIN INIT INFO
# Provides:          xenPoolHostShutdownManager
# Required-Start:    $remote_fs $syslog
# Required-Stop:     $remote_fs $syslog
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: It is used to clean XCP host configurations when it is forgotten by pool menbers
# Description:       This service is used to manage a host configurations all together with the cloudstack consolidaiton plugin
### END INIT INFO


case "$1" in
    start)
        echo "Starting xenPoolHostShutdownManager"
        update-rc.d xenPoolHostShutdownManager remove
        ;;
    stop)
        echo "Stopping xenPoolHostShutdownManager"
        logger "resetting host to be its own master"
        echo "master" > /etc/xcp/pool.conf
        logger "cleaning XCP database"
        rm -rf /var/lib/xcp/state.db
        logger "end of factory reset"
        ;;
    *)
        echo "Usage: /etc/init.d/xenPoolHostShutdownManager start|stop"
        exit 1
        ;;
esac

exit 0
