#!/usr/bin/perl

$i = 0;

while(<STDIN>) {
	@a = split " ", $_;
	$i++;
	$t = 3 * $i;

	$a[5] =~ s/m//g;

	print "$t $a[5] \n";

}

