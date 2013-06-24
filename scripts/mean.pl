#!/usr/bin/perl

use POSIX;

# all delay values
@numbers;

while(<STDIN>)
{
	# split the input into X rows
	@A = split " ", $_;
	# compute the delay: time of completion - time of issuance
	$B = $A[3] - $A[1];
	# delay in milliseconds
	$C = $B / 1000000;
	# round the delay to milliseconds
	$D = ceil($C);
	# increase the delay for an appropriate delay time
	$numbers[$D]++;
	# increase the total number of packets
	$i++;
}

# for all delay values between 1 ms and 3 secs
for($j = 1; $j < 3000; $j++) {
	if($numbers[$j] == "") {
		$numbers[$j] = 0;
	}
	
	# print the delay distribution (normalize by dividing through the number of packets)
	$dist = $numbers[$j] / $i;

	print "$j $dist \n";
}
