#!/bin/sh
#
#
# JBoss, Home of Professional Open Source. Copyright 2011, Red Hat, Inc., and
# individual contributors as indicated by the @author tags. See the
# copyright.txt file in the distribution for a full listing of individual
# contributors.
# 
# This is free software; you can redistribute it and/or modify it under the
# terms of the GNU Lesser General Public License as published by the Free
# Software Foundation; either version 2.1 of the License, or (at your option)
# any later version.
# 
# This software is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
# details.
# 
# You should have received a copy of the GNU Lesser General Public License
# along with this software; if not, write to the Free Software Foundation,
# Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
# site: http://www.fsf.org.


# File: run.sh
#
# Created on Nov 1, 2011 at 10:30:28 AM 
#
# @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>


type=$1;
mode=$2;
port=$3;

DIRNAME=`dirname "$0"`

# Read an optional running configuration file
if [ "x$RUN_CONF" = "x" ]; then
    RUN_CONF="$DIRNAME/conf/run.conf"
fi
if [ -r "$RUN_CONF" ]; then
    . "$RUN_CONF"
fi

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
        JAVA="$JAVA_HOME/bin/java"
    else
        JAVA="java"
    fi
fi

XNIO3_NIO2_HOME=`cd "$DIRNAME/."; pwd`

# Display our environment
echo ""
echo "========================================================================="
echo ""
echo "  XNIO3 & NIO.2 Server Bootstrap Environment"
echo ""
echo "  XNIO3_NIO2_HOME: $XNIO3_NIO2_HOME"
echo ""
echo "  JAVA: $JAVA"
echo ""
echo "  JAVA_OPTS: $JAVA_OPTS"
echo ""
echo "========================================================================="
echo ""
echo ""

if [ "x$type" = "x" ]; then
	printf "ERROR: you should provide a type of the server to run (xnio3 or nio2)\n";
	printf " --> Example: sh run.sh xnio3 async 8080\n";
	exit -1;
fi

if [ "x$mode" = "x" ]; then
	printf "ERROR: you should provide a running mode of the server (sync or async)\n";
	printf " --> Example: sh run.sh xnio3 async 8080\n";
	exit -1;
fi

if [ "x$port" = "x" ]; then
	printf "ERROR: you should provide a port number on which the server will binds (e.g., 8080)\n";
	printf " --> Example: sh run.sh xnio3 async 8080\n";
	exit -1;
fi

printf "  Running server application with parameters \n";
printf "  \tType: $type\n";
printf "  \tMode: $mode\n";
printf "  \tPort: $port\n";
echo "";

java $JAVA_OPTS -jar target/nio2-xnio3-test.jar $type $mode $port

#eval \"$JAVA\" $JAVA_OPTS \
#         -jar target/nio2-xnio3-test.jar $type $mode $port
#         "$@"
#      XNIO3_NIO2_STATUS=$?
