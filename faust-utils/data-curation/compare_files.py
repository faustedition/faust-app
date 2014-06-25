#!/usr/bin/env python

import os.path
import sys

def list_in(path):
    result = set()
    with open(path) as list_file:
        for line in list_file:
            result.add(os.path.normpath(line))
    return result

def main():
    if len(sys.argv) != 3:
        print 'Usage: compare_files.py list1 list2'
    else:
        set1 = list_in(sys.argv[1])
        set2 = list_in(sys.argv[2])

        print 'Only in', sys.argv[1]
        for path in set1.difference(set2):
            print path
        print

        print 'Only in', sys.argv[2]
        for path in  set2.difference(set1):
            print path


if __name__ == "__main__":
    main()
