import requests, urllib, os, os.path, sys, tempfile, shutil
from subprocess import call, check_call

manuscript_urls = [
    # "artige Handschriften"

    'http://localhost:8080/faustedition/document//paralipomena/gsa_390140.xml',
    'http://localhost:8080/faustedition/document//faust/2.4/gsa_390595.xml',
    'http://localhost:8080/faustedition/document//faust/2.5/gsa_390183.xml',
    'http://localhost:8080/faustedition/document//paralipomena/gsa_391369.xml',
    'http://localhost:8080/faustedition/document//verschiedenes/gsa_390875.xml',
    'http://localhost:8080/faustedition/document//paralipomena/gsa_391532.xml',
    'http://localhost:8080/faustedition/document//paralipomena/gsa_391374.xml',
    'http://localhost:8080/faustedition/document//faust/2.2/gsa_390835.xml',
    'http://localhost:8080/faustedition/document//verschiedenes/gsa_390076.xml',
    'http://localhost:8080/faustedition/document//faust/2.4/gsa_390017.xml',
    'http://localhost:8080/faustedition/document//paralipomena/gsa_390403.xml',
    'http://localhost:8080/faustedition/document//faust/2.4/gsa_390157.xml',
    'http://localhost:8080/faustedition/document//paralipomena/gsa_389808.xml',
    'http://localhost:8080/faustedition/document//faust/2.1/gsa_390689.xml',
    'http://localhost:8080/faustedition/document//paralipomena/gsa_390093.xml',
    'http://localhost:8080/faustedition/document//paralipomena/gsa_390900.xml',
    'http://localhost:8080/faustedition/document//paralipomena/gsa_390826.xml',
    'http://localhost:8080/faustedition/document//faust/1/gsa_390822.xml',
    'http://localhost:8080/faustedition/document//faust/2/gsa_390777.xml',
    'http://localhost:8080/faustedition/document//paralipomena/gsa_391371.xml',
    'http://localhost:8080/faustedition/document//faust/2.2/gsa_390773.xml'
    ]

latex_header = """\\documentclass[11pt,oneside]{book} 
\\usepackage{makeidx}
\\usepackage{graphicx}
\\usepackage[german]{babel} 
\\begin{document} 
\\author{Johann Wolfgang Goethe} 
\\title{Faust. Historisch-kritische Ausgabe} 
\\date{\\today} 
\\maketitle
\\frontmatter 
\\setcounter{secnumdepth}{0}
\\setcounter{tocdepth}{1}
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
        # print "   seite: " + str(mu['transcript'] if 'transcript' in mu else '--')
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
        out_filepath = generate_out_filepath(page_url, tmp_dir)
        print "rendering page ", pagenum, ": ", page_url
        if not os.path.exists(out_filepath):
            print "   (rendering to      " + out_filepath  + ")"
            check_call(['phantomjs', 'render-transcript.js', url + '?view=transcript-bare#' + str(i+1), out_filepath]) 
        else:
            print "   (already exists at " + out_filepath + ")"
        
def generate_latex(manuscript_urls, tmp_dir):
    result = ''
    for url in manuscript_urls:
        doc_name = get_doc_name(url)
        result = result + '\clearpage\n'
        result = result + '\section{' + doc_name + '}\n'
        for (i, page_url) in enumerate(get_pageurls(url)):
            pagenum = i + 1
            if pagenum > 1:
                result = result + '\clearpage\n'
            result = result + '\subsection{Seite ' + str(pagenum) + "}\n"
            result = result + '\\vfill{}\n'
            # TODO hack
            if "self/none"  in page_url:
                result = result + "[Leere Seite]"
            else: 
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

print "writing latex to " + latex_filename
latex_out.write(latex_header)
latex_out.write(generate_latex(manuscript_urls, tmp_dir))
latex_out.write(latex_footer)
latex_out.close()

os.chdir(latex_tmp_dir)
# twice for toc indexing
check_call(['pdflatex', '-output-directory ' + latex_tmp_dir, latex_filename])
check_call(['pdflatex', '-output-directory ' + latex_tmp_dir, latex_filename])
shutil.copyfile(os.path.join(latex_tmp_dir, "faust.pdf"), pdf_result)



