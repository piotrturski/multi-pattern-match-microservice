# generates files with post request of different sizes. those files can be used by ab
TARGET_DIR=/tmp
mkdir -p $TARGET_DIR
for i in 2 10 24 25
do
   yes `head -n -1 ../main/resources/phrases` | head -n $i > $TARGET_DIR/ab-post-${i}.txt

done
