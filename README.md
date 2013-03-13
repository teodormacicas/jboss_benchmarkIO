Benchmarking NIO.2, XNIO.3 and Netty as three different Java I/O frameworks. 
This will be done by implementing three simple client-server applications using the above-mentioned 
frameworks. We want to measure the response time from client perspective, CPU and memory usage 
on the server varying the number of requests per second and the number of concurrent 
clients. 

This project is based on a previous work of Nabil Benothman. He compared the NIO.2 
and XNIO.3, but not Netty. Furthermore, some trends could not be explained. Apart that 
we target to create a small automated testing framework, rather than running everything 
manually. 

The context of this work is the workshop from University of Neuchatel part of our M.Sc. studies 
in Computer Science department. 
