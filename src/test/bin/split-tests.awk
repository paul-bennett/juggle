#!/usr/bin/awk -f

# Divide input into three parts:
#  1. Prose (to be thrown away)
#  2. Invocations (command-line args to Juggle)
#  3. Output
#
# What we're interested is pairs of invocations and output.
# Ultimately we want to run Juggle with each commend-line
# and compare the generated output to that recorded in the file.

# First, see if we can generate an AWK script to annotate each line.

# Boundaries:
#   Invocation: /^\$ juggle/ .. /[^\\]$/
#   Output:     (end of invocation) .. /^\$/ (but not including that line)
#   Prose:      everything else


function fname(n, suffix) { return prefix "-" n "." suffix }
function cmdfile(n) { return fname(n, "sh")  }
function outfile(n) { return fname(n, "out") }

BEGIN 			{ state=1; n=0; prefix="juggle-test" }

state==1 && /^\$ juggle/{ state=2; n++; $0=substr($0, 3, length($0)-3+1)      }
state==2 && /[^\\]$/	{ state=3; print >cmdfile(n); close(cmdfile(n)); next }
state==2                {          print >cmdfile(n);                         }
state==3 && /^\$$/	{ state=1;                    close(outfile(n)); next }
state==3		{          print >outfile(n);                    next }


# Later..
#   expected_output=`mktemp -t expected`
#   actual_output=`mktemp -t actual`
#   echo ... > $expected_output
#   juggle $args > $actual_output
#   diff $expected_output $actual_output
#   result=$?
#   rm $expected_output $actual_output

