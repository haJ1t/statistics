#!/bin/zsh
# Copies the benchmark tools, agent configs and results to DEST (default: next to the user's groupA folder).
# The copy has the layout of TAG/arena; its scripts use paths relative to their own location.
SRC=/Users/halit/Desktop/statistics/TAG/arena
DEST=${1:-/Users/halit/Desktop/statistics/groupA_code/experiments_v3}
mkdir -p $DEST/results $DEST/src
cp -R $SRC/agents $DEST/
cp -R $SRC/src/arena $SRC/src/check $SRC/src/refv2 $DEST/src/
cp $SRC/*.py $DEST/
cp $SRC/*.md $DEST/ 2>/dev/null
cp $SRC/results/*.csv $SRC/results/*.log $SRC/results/*.txt $DEST/results/ 2>/dev/null
for f in run.sh batch.sh build.sh make_tables.sh make_results.sh; do
  sed -e 's|^cd /Users/halit/Desktop/statistics/TAG/arena/results$|cd "$(dirname "$0")/results"|' \
      -e 's|^cd /Users/halit/Desktop/statistics/TAG$|cd "$(dirname "$0")/.."|' \
      -e 's|^SNAPROOT=.*|SNAPROOT=${TMPDIR:-/tmp}/arena-snap|' \
      -e 's|^export PATH=/opt/homebrew/opt/openjdk/bin:\$PATH$|# needs Java 21+ on the PATH|' \
      $SRC/$f > $DEST/$f
  chmod +x $DEST/$f
done
rm -f $DEST/package.sh
echo "packaged into $DEST"
grep -rn "/Users/halit\|claude-501" $DEST --include='*.sh' --include='*.py' --include='*.java' || echo "no machine-specific paths left"
