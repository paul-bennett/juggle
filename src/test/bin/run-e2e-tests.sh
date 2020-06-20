#!/bin/sh

# Driver for E2E tests.
#
# Extracts sample invocations and expected output from stdin,
# re-runs each and compares actual with expected output.
#
# $ src/test/bin/run-e2-tests.sh < README.md

bin_dir="${0%/*}"
top_dir="${bin_dir}/../../.."
gradle=./gradlew

tmp_dir=`mktemp -d -t juggle-e2e`
prefix="$tmp_dir"/juggle-test

awk -f "${bin_dir}/split-tests.awk" prefix="${prefix}"

# On macOS use JDK13 because this version of Gradle barfs on JDK14.
gradle_ver=`${gradle} --version | grep Gradle | cut -d\  -f2`
if [ `uname -s` = "Darwin" -a "${gradle_ver}" = "6.1.1" ]
then
    export JAVA_HOME=`/usr/libexec/java_home -v 13`
fi

#juggle() {
#    echo >&2 "))))" "${gradle}" -q run --args="$@"
#    "${gradle}" -q run --args="$*"
#}

temp="${prefix}-out"

n=1
until [ ! -f "${prefix}-${n}.sh" ]
do
    cmd_file="${prefix}-${n}.sh"
    out_file="${prefix}-${n}.out"
    args=`cat ${cmd_file} | sed '1s/[^ ]* *//'`
    ${gradle} -q run --args="${args}" 2>&1 \
	| diff -u - --label "actual output" "${out_file}" > "${temp}"
    #    . "${cmd_file}" | diff - "${out_file}"

    if [ $? -ne 0 ]; then
	echo "========================== FAILED TEST =========================="
	cat "${cmd_file}"
	echo "-----------------------------------------------------------------"
	cat "${temp}"
	echo
	echo
    else
	rm "${cmd_file}" "${out_file}"
    fi

    let n+=1
done

rm "${temp}"
rmdir "$tmp_dir"
