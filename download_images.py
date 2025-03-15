from requests import request
from bs4 import BeautifulSoup
from collections import deque
from time import sleep
import os
from pathlib import Path
import json

def parse_imgs(soup):
    imgs = soup.find_all('img', class_='__posterbox')
    res = []
    for img in imgs:
        res.append(img['data-src'])
    return list(set(res))


main_prefix = 'ranobehub.org'
description_prefix = 'ranobehub.org_ranobe_'

def check_if_description_file(fname):
    parts = fname.split('_')
    return len(parts) == 3 and parts[0] == main_prefix and parts[1] == 'ranobe' and fname.endswith(".html")

imgs_directory = 'imgs/all/'
Path(imgs_directory).mkdir(parents=True, exist_ok=True)


def img_path(ranobe_id, ind):
    return imgs_directory + 'img_' + str(ranobe_id) + '_' + str(ind) + '.jpg'

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
        

        html_doc = read_entire_file(path)
        soup = BeautifulSoup (html_doc, 'html.parser')

        img_hrefs = parse_imgs(soup)
        for i in range(len(img_hrefs)):
            url = img_hrefs[i]
            try:
                response = request('GET', url)
            except:
               print('error', ranobe_id, url)
            finally:
                sleep(0.5)

            #print('response', response)

            path = img_path(ranobe_id, i)
            img_data = response.content
            with open(path, 'wb') as handler:
                handler.write(img_data)
        #exit(0)
            #with open(path, 'w', encoding='utf-8') as outfile:
            #    json.dump(params, outfile, indent=4, ensure_ascii=False)
        

        cnt += 1
        if cnt % 50 == 0:
            print('cnt =', cnt)
            
print('cnt =', cnt)
