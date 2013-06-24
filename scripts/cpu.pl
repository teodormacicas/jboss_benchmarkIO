#!/usr/bin/perl

# sum of values
$sum = 0;
# sum of squared values
$sumsq = 0;
# number of all entries
$i = 0;

while(<STDIN>) {
	# split into an array
	@A = split "," , $_;
	# remove the "%id," from the fourth column
	$A[3] =~ s/%id,//g;
	# add to the sum
	$sum += $A[3];
	# add to the squared sum
	$sumsq += ($A[3] * $A[3]);
	# increase the number of entries
	$i++;

}

# compute variance
$var = ($sumsq/$i) - (($sum/$i) * ($sum/$i));
# compute standard deviation
$sigma = sqrt($var);

# compute load = 100% - idle [%]
$ex = 100 - $sum / $i;

print "$ex $sigma";
