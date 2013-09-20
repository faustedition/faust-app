import query, faust, os.path, sys, os, shutil

def destination(file):
    rel_f = os.path.relpath(file, faust.xml_dir)
    return os.path.join(faust.xml_dir, 'attic', rel_f)

if __name__ == "__main__":

    deleatur_transcripts = query.matches(faust.transcript_files(), query.deleatur_xp)

    if '-e' in sys.argv:
        print "executing"
        for f in deleatur_transcripts:
            print '         ' + f
            print '-->' + destination(f)
            print ''

            dest_dir = os.path.dirname(destination(f))
            print dest_dir
            if not os.path.isdir(dest_dir):
                os.makedirs(dest_dir)
            shutil.move (f, destination(f))
    else:
        for f in deleatur_transcripts:
            print '         ' + f
            print '-->' + destination(f)
            print ''

        print "To execute, call with -e option"


