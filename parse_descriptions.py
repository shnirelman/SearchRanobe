from requests import request
from bs4 import BeautifulSoup
from collections import deque
from time import sleep
import os
from pathlib import Path
import json

def parse_description(soup):
    res = dict()
    
    h1 = soup.find('h1')
    name = h1.text.strip()
    res.update({'name': name})
    
    h2 = soup.find_all('h2')[1]
    other_names = []
    for x in h2.text.split('/'):
        other_names.append(x.strip())
    res.update({'other_names': other_names})
    
    
    description_div = soup.find('div', class_='book-description__text')
    description = description_div.text
    res.update({'description': description})
    
    tags = set()
    tmp = soup.find_all('div', class_='book-tags')

    for div in tmp:
        for link in div.find_all('a'):
            tags.add(link.text.strip())
    res.update({'tags': list(tags)})

    rating_div = soup.find('div',
                           attrs={'data-tippy-content':'Рейтинг'})
    rating = '?'
    if rating_div != None:
        rating = rating_div.text.strip()
    res.update({'rating': rating})

    translation_quality_div = soup.find('div', attrs={'data-tippy-content':'Качество перевода'})
    translation_quality = '?'
    if translation_quality_div != None:
        translation_quality = translation_quality_div.text.strip().split()[0]
    res.update({'translation_quality': translation_quality})
    
    return res


main_prefix = 'ranobehub.org'
description_prefix = 'ranobehub.org_ranobe_'

def check_if_description_file(fname):
    parts = fname.split('_')
    return len(parts) == 3 and parts[0] == main_prefix and parts[1] == 'ranobe' and fname.endswith(".html")

json_description_directory = 'json/description/'
Path(json_description_directory).mkdir(parents=True, exist_ok=True)

def description_json_path(ranobe_id):
    return json_description_directory + 'description_' + str(ranobe_id) + '.json'

def read_entire_file(fname):
    with open(fname, 'r', encoding="utf-8") as f:
        content = f.read()
    return content

cnt = 0
for fname in os.listdir('html/'):
    path = 'html/' + fname
    if os.path.isfile(path) and check_if_description_file(fname):
        parts = fname.split('_')

        try:
            ranobe_id = int(parts[2].replace('.', '-').split('-')[0])
        except:
            continue
        
        json_path = description_json_path(ranobe_id)

        if os.path.isfile(json_path):
            continue

        html_doc = read_entire_file(path)
        soup = BeautifulSoup (html_doc, 'html.parser')

        params = parse_description(soup)
        params.update({'ranobe_id': ranobe_id})
        with open(json_path, 'w', encoding='utf-8') as outfile:
            json.dump(params, outfile, indent=4, ensure_ascii=False)

        cnt += 1
        if cnt % 50 == 0:
            print('cnt =', cnt)
            
print('cnt =', cnt)
