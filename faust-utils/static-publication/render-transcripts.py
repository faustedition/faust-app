import requests, urllib, os, os.path, sys, tempfile, shutil
from subprocess import call, check_call

manuscript_urls = [
    # 'http://localhost:8080/document/faust/2.5/gsa_390883.xml',
    'http://localhost:8080/document/archival/fdh_frankfurt/Hs-6626.xml',
    'http://localhost:8080/document/faust/2.1/gsa_390143.xml',
    #'http://localhost:8080/document/faust/2/gsa_391098.xml'
    ]

latex_header = """\\documentclass[11pt]{book} 
\\usepackage{makeidx}
\\usepackage{graphicx}
\\usepackage[german]{babel} 
\\begin{document} 
\\author{Johann Wolfgang von Goethe} 
\\title{Faust. Historisch-kritische Ausgabe} 
\\date{\\today} 
\\maketitle
\\frontmatter 
\\tableofcontents 
\\mainmatter 
\\chapter{Handschriften}
"""

latex_footer = """
\\backmatter 
\\printindex
\\end{document}
"""


def extract_pages(mu):
    result = [];
    if mu['type'] == 'page':
        result.append(mu)
    for child in mu['contents']:
        result.extend(extract_pages(child))
    return result

def get_pageurls(url):
    answer = requests.get(url).json()
    answer_pages = extract_pages(answer)
    return [a_page['transcript']['source'] for a_page in answer_pages if 'transcript' in a_page]

def get_doc_name(url):
        answer = requests.get(url).json()
        return answer['name']

def quote_filename(filename):
    return urllib.quote_plus(filename.encode('utf-8').replace('.', '_') + '.png').replace('%', '_')

def generate_out_filepath(page_url, tmp_dir):
        out_filename = quote_filename(page_url)
        return os.path.join(tmp_dir, 'graphics',  out_filename)

def render_document(url, tmp_dir):    
    for (i, page_url) in enumerate(get_pageurls(url)):
        #pagenumbers starting from 1
        pagenum = i + 1 
        out_filepath = generate_out_filepath(url, tmp_dir)
        print "rendering page ", pagenum, ": ", page_url
        if not os.path.exists(out_filepath):
            check_call(['phantomjs', 'render-transcript.js', url + '?view=transcript-bare#' + str(i+1), out_filepath]) 
        else:
            print "(already exists)"
        
def generate_latex(manuscript_urls, tmp_dir):
    result = ''
    for url in manuscript_urls:
        doc_name = get_doc_name(url)
        result = result + '\section{' + doc_name + '}\n'
        for page_url in get_pageurls(url):
            result = result + '\clearpage\n'
            result = result + '\includegraphics[width=\\textwidth,height=\\textheight,keepaspectratio]{' + generate_out_filepath(page_url, tmp_dir)  +'}\n'
    return result



if len(sys.argv) < 2 or len(sys.argv) > 3:
    print 'usage: render-transcripts.py pdf_result [tmp_dir]'
    print '   tmp_dir caches rendered graphics to be reused'
    exit(-1)

pdf_result = os.path.abspath(sys.argv[1])
tmp_dir = os.path.abspath(sys.argv[2]) if len(sys.argv) > 2 else tempfile.mkdtemp()

if not os.path.isdir(tmp_dir):
    os.mkdir(tmp_dir)

for url in manuscript_urls:
    render_document(url, tmp_dir)

latex_tmp_dir = tempfile.mkdtemp()
latex_filename = os.path.join(latex_tmp_dir, 'faust.tex')
latex_out = open(latex_filename, 'w')

latex_out.write(latex_header)
latex_out.write(generate_latex(manuscript_urls, tmp_dir))
latex_out.write(latex_footer)

os.chdir(latex_tmp_dir)
check_call(['pdflatex', latex_filename])
shutil.copyfile(latex_filename, pdf_result)



