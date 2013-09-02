#!/usr/bin/env python
#
# synchronize remote folders of files according to precalculated md5 tables

import sys
import os.path

def read_table(file_path):
    result = dict()
    with open(file_path) as table_file:
        for line in table_file:
            hashval = line[:line.find('  ')]
            path = line[line.find('  ') + 2:-1]
            if (hashval in result):
                # print 'duplicate in', file_path
                # print '     in entries'
                # print '      ', path
                # print '      ', result[hashval]
                # exit(-1)
                pass
            result[hashval] = path
    return result

def main():
    if len(sys.argv) != 3:
        print 'Usage: move_facsimiles.py target.md5 actual.md5'
    else:
        target_table = read_table(sys.argv[1])
        actual_table = read_table(sys.argv[2])

        for (hash_value, old_path) in actual_table.items():
            if hash_value in target_table:
                new_path = target_table[hash_value]
                if (old_path != new_path):
                    print 'mkdir -p ', os.path.dirname(new_path)
                    print "mv '" +  old_path + "' '" + new_path + "'\n"
                    print "ls '" + old_path + "'"
                    print "ls '" + new_path + "'"
                    print 'read -p "press [enter]"'
                    pass
                else:
                    # print 'identical:', old_path
                    pass
            else:
                # print 'only in', sys.argv[2], ':', new_path
                pass


            



        
source_md5s = dict()

if __name__ == '__main__':
    main()
