/**
 * JBoss, Home of Professional Open Source. Copyright 2012, Red Hat, Inc., and
 * individual contributors as indicated by the @author tags. See the
 * copyright.txt file in the distribution for a full listing of individual
 * contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

@author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>



This project is a maven based project. So to build the project you should have
maven installed on your machine. If you do not have it already installed, you can 
download it form maven website:

    http://maven.apache.org/download.html
    
    
1) To build the project, use the script "build.sh"

2) To run the project use the script "run.sh". This script requires 3 parameters:

   * Type: the server type, i.e., "xnio3" or "nio2". This parameter is required
   * Mode: the server mode, i.e., "sync" or "async". This parameter is required
   * Port: the port number to which the server will binds. This parameter is
           optional, the default value is 8080
 
   Example: install_dir/nio2-xnio3-test$ sh run.sh nio2 async 8001
   
   
   
   
   
