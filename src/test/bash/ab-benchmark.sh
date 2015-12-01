# sample rest benchmark. assumes existence of file with post data. file can be created by ./make-post-files.sh
JSON_FILE=/tmp/ab-post-24.txt

ab -T 'text/plain' -r -p $JSON_FILE -l -t 120 -c 4 localhost:8080/find-matches

# newer versions on ab accept -l flag and produce cleaner output