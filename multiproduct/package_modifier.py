import os
import shutil
from sys import argv
from datetime import datetime

LAUNCHER_VARIANT_ROOTS = [
    'main',
    'colorphoneCommon',
    'colorphone',
    'colorphoneYingyongbao',
    'colorphoneJinritoutiao',
]

def is_varient_root(root):
    return os.path.exists(root + '/src') and (str.find(root, 'colorphone') >= 0 or str.find(root, 'main') >= 0)


def replace_str_in_file(path, str_src, str_dst):
    if path.find('strings.xml') >= 0:
        return
    with open(path) as f:
        s = f.read()
        s = s.replace(str_src, str_dst)
        with open(path, 'w') as fi:
            fi.write(s)


def replace_str_in_dir(directory, str_src, str_dst):
    for dname, dirs, files in os.walk(directory):
        for fname in files:
            fpath = os.path.join(dname, fname)
            replace_str_in_file(fpath, str_src, str_dst)


if __name__ == '__main__':
    path = './'
    package_name = argv[1]
    application_id = argv[2]
    print package_name
    print application_id
    if package_name != application_id:

        start = datetime.now()

        print 'package modifier - from ' + package_name + ' to ' + application_id

        if application_id.find('themelab') >= 0:
            directory = 'common'
            print 'src - ' + path + directory + '/src'
            replace_str_in_dir(path + directory + '/src', package_name, application_id)
            print 'rs - ' + path + directory + '/rs'
            replace_str_in_dir(path + directory + '/rs', package_name, application_id)
            print 'res - ' + path + directory + '/res'
            replace_str_in_dir(path + directory + '/res', package_name, application_id)

            strs1 = application_id.split('com.themelab.launcher.')
            strs2 = package_name.split('com.themelab.launcher.')
            varient_id = strs1[1] if len(strs1) == 2 else strs2[1]
            directory = 'themes' + '/' + varient_id
            print 'src - ' + path + directory
            replace_str_in_dir(path + directory + '/src', package_name, application_id)
            print 'rs - ' + path + directory + '/rs'
            replace_str_in_dir(path + directory + '/rs', package_name, application_id)
            print 'res - ' + path + directory + '/res'
            replace_str_in_dir(path + directory + '/res', package_name, application_id)

            replace_str_in_file('./themes/common/AndroidManifest.xml', package_name, application_id)
        else:
            for directory in LAUNCHER_VARIANT_ROOTS:
                print 'src - ' + path + directory + '/src'
                replace_str_in_dir(path + directory + '/src', package_name, application_id)
                print 'rs - ' + path + directory + '/rs'
                replace_str_in_dir(path + directory + '/rs', package_name, application_id)
                print 'res - ' + path + directory + '/res'
                replace_str_in_dir(path + directory + '/res', package_name, application_id)

            replace_str_in_file('./main/AndroidManifest.xml', package_name, application_id)

        print 'proguard'
        replace_str_in_file('./proguard.pro', package_name, application_id)
        print 'manifest'
        replace_str_in_file('./AndroidManifest.xml', package_name, application_id)

        time_diff = datetime.now() - start
        print 'complete. time cost ' + str(time_diff)
