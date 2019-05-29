cd "$(dirname "$0")"

# 0. Make directories
mkdir ../outputs
mkdir ../outputs/"$1"
mkdir ../outputs/"$1"/configs
mkdir outapk

# 1. Run AndResGuard with given configuration (eg. "config-launcherSP.xml")
cp ../app/build/outputs/apk/"$1"/release/app*.apk ../app/build/outputs/apk/"$1"/release/"$1"-release.apk
java -jar AndResGuard-cli-1.2.3.jar ../app/build/outputs/apk/"$1"/release/"$1"-release.apk -config config-"$1".xml -7zip ./SevenZip-osx-x86_64.exe -zipalign ./zipalign -out outapk

# 2. Copy shrinked APK to output directory
mv outapk/"$1"-release_signed_7zip_aligned.apk ../outputs/"$1"/app-release.apk

# 3. Copy code & resource mapping files to output derictory
mv outapk/resource_mapping_"$1"-release.txt ../outputs/"$1"/mapping-res.txt
mv ../app/build/outputs/mapping/"$1"/release/mapping.txt ../outputs/"$1"/mapping-code.txt

# 4. Cleanup
# rm -r outapk
