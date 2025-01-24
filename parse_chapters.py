from requests import request
from bs4 import BeautifulSoup
from collections import deque
from time import sleep
import os
from pathlib import Path
import json

def parse_chapter(soup):
    res = dict()
    
    h1 = soup.find('h1')
    name = h1.text.strip()
    res.update({'name': name})
    
    text_div = soup.find('div', class_='ui text container', attrs={'data-container': True})
    text = '\n'.join(map(lambda p: p.text, text_div.find_all('p')))
    res.update({'text': text})
    
    return res


main_prefix = 'ranobehub.org'
chapter_prefix = 'ranobehub.org_ranobe_'

def check_if_chapter_file(fname):
    parts = fname.split('_')
    return len(parts) == 5 and parts[0] == main_prefix and parts[1] == 'ranobe' and fname.endswith(".html")

json_chapter_directory = 'json/chapter/'
Path(json_chapter_directory).mkdir(parents=True, exist_ok=True)

def chapter_json_path(ranobe_id, volume_id, chapter_id):
    return json_chapter_directory + str(ranobe_id) + '_chapter_' + str(volume_id) + '_' + str(chapter_id) + '.json'

def read_entire_file(fname):
    with open(fname, 'r', encoding="utf-8") as f:
        content = f.read()
    return content

cnt = 0
for fname in os.listdir('html/'):
    path = 'html/' + fname
    
    if os.path.isfile(path) and check_if_chapter_file(fname):
        parts = fname.split('_')
        
        try:
            ranobe_id = int(parts[2].replace('.', '-').split('-')[0])
            volume_id = int(parts[3].replace('.', '-').split('-')[0])
            chapter_id = int(parts[4].replace('.', '-').split('-')[0])
        except:
            continue
        
        json_path = chapter_json_path(ranobe_id, volume_id, chapter_id)

        if os.path.isfile(json_path):
            continue

        html_doc = read_entire_file(path)
        soup = BeautifulSoup (html_doc, 'html.parser')

        params = parse_chapter(soup)
        params.update({'ranobe_id': ranobe_id, 'volume_id': volume_id, 'chapter_id': chapter_id})
        with open(json_path, 'w', encoding='utf-8') as outfile:
            json.dump(params, outfile, indent=4, ensure_ascii=False)

        cnt += 1
        if cnt % 50 == 0:
            print('cnt =', cnt)
                
print('cnt =', cnt)
