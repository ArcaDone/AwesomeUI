#!/bin/bash

function run_step {
    STEP_COMMAND_LINE=$1
    STEP_NAME=$2

    eval $STEP_COMMAND_LINE

    EXIT_CODE=$?

    if [ $EXIT_CODE -ne 0 ]; then
        echo "***********************************************"
        echo "      🔴 🔴 🔴 🔴 🔴 🔴 🔴 🔴  "
        echo "           $STEP_NAME failed                 "
        echo " Please fix the above issues before committing "
        echo "      🔴 🔴 🔴 🔴 🔴 🔴 🔴 🔴  "
        echo "***********************************************"
        echo
        exit $EXIT_CODE
    fi
}

echo
echo " Start PRE-TEST CHECKS ⏳"
echo

echo "🧽  First, clean up all...."

run_step "./gradlew clean > /dev/null" "clean"

echo "🧹  ...then use ktlint..."
run_step "./gradlew ktlintformat > /dev/null" "ktlintformat"

echo
echo " PRE-TEST CHECKS DONE ✅   "
echo
#echo "Now it's time to test all!"
#
#run_step "./gradlew executeUnitTests" "executeUnitTests"
#
#
#echo
#echo "     ⚠️ ⚠️ ⚠️ ⚠️ ⚠️ ⚠️ ⚠️ ⚠️ ⚠️ ⚠️  "
#echo "And last but not least.. Coverage! Hold on tight, we have to be above 90% 😈"
#echo "     ⚠️ ⚠️ ⚠️ ⚠️ ⚠️ ⚠️ ⚠️ ⚠️ ⚠️ ⚠️  "
#
#run_step "./gradlew koverHtmlReport"
#run_step "./gradlew koverVerify"

echo
echo
echo "🟢 🟢 🟢 🟢 🟢"
echo "✅ 🧪 ✨ 🚀 We are Ready to merge! 🎉 🍾 🥳"
echo "🟢 🟢 🟢 🟢 🟢 OK ! Ready to merge!"
echo
echo
