from requests import request
from bs4 import BeautifulSoup
from collections import deque
from time import sleep
from pathlib import Path

start = "https://ranobehub.org"
q = deque()
q.append(start)
used = {start}

forbidden_substrings = ['#', 'redirect', 'user', 'translator',
                        'author', 'tag', 'blog', 'country',
                        'moderations', 'character']
required_substrings = ['ranobehub.org']

def url_clear(url):
    return url.replace('https://', '').replace('/', '_').replace('?', '__').replace('&', '_').replace('=', '_') + '.html'

def file_name(url):
    res = 'html/' + url_clear(url)
    print(res)
    return res

def error_file_name(url):
    res = 'html/error/' + url_clear(url)
    print(res)
    return res

def is_file_exist(fname):
    return Path(fname).is_file()

def read_entire_file(fname):
    with open(fname, 'r', encoding="utf-8") as f:
        content = f.read()
    return content

def save_html(fname, html_doc):
    with open(fname, 'w', encoding="utf-8") as f:
        f.write(html_doc)

Path('html/error').mkdir(parents=True, exist_ok=True)

cnt = 0
while q and cnt < 10000:
    cnt += 1
    print("cnt =", cnt)
    url = q.popleft()
    print(url)
    html_doc = ''
    fname = file_name(url)
    if is_file_exist(fname):
        html_doc = read_entire_file(fname)
    else:
        error_fname = error_file_name(url)
        if is_file_exist(error_fname):
            continue

        try:
            response = request('GET', url)
        except:
            save_html(error_fname, "error")
        finally:
            sleep(0.5)
            
        if response.status_code != 200:
            save_html(error_fname, "error")
            continue
        html_doc = response.text
        save_html(fname, html_doc)
    
    soup = BeautifulSoup (html_doc, 'html.parser')

    for link in soup.find_all('a', href=True):
        new_url = link.get('href')

        flag = False
        for substr in forbidden_substrings:
            if new_url.find(substr) != -1:
                flag = True
                break
        
        if flag:
            continue
        
        for substr in required_substrings:
            if new_url.find(substr) == -1:
                flag = True
                break
        
        if flag:
            continue
        
        if new_url not in used:
            print("add url :", new_url)
            used.add(new_url)
            q.append(new_url)

    print('end while')
