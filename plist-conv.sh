chmod +x ../jacoder

echo "Converting files below to plist archive:$1"
for file in `find ./build/intermediates/ -type f -name "*.plist"`
do
    echo $file
    ../jacoder -e $file ${file/.plist/.sp}
    rm $file
done
