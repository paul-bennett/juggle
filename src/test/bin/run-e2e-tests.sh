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

temp="${prefix}-out"

n=1
until [ ! -f "${prefix}-${n}.sh" ]
do
    cmd_file="${prefix}-${n}.sh"
    out_file="${prefix}-${n}.out"
    # First sed expression removes command name; second strips continuation \s
    args=`cat ${cmd_file}				\
		| sed -e '1s/[^ ]* *//' -e 's/\\\\$//'	\
		| tr \\\\n ' '`
    ${gradle} -q run --args="${args}" 2>&1 \
        | diff -u --label "expected output" --label "actual output" "${out_file}" - > "${temp}"

    if [ $? -ne 0 ]; then
        echo "============================== FAILED TEST =============================="
        /bin/echo -n '$ '
        cat "${cmd_file}"
        echo "-------------------------------------------------------------------------"
        cat "${temp}"
        echo
        echo
    fi

    rm "${cmd_file}" "${out_file}"

    let n+=1
done

rm "${temp}"
rmdir "$tmp_dir"
