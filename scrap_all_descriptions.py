from requests import request
from bs4 import BeautifulSoup
from collections import deque
from time import sleep

def save_ranobe_page(soup):
    h1 = soup.find('h1')
    name = h1.text.strip()
    
    h2 = soup.find_all('h2')[1]
    other_names = []
    for x in h2.text.split('/'):
        other_names.append(x.strip())
    
    description_div = soup.find('div', class_='book-description__text')
    description = description_div.text

    tags = set()
    tmp = soup.find_all('div', class_='book-tags')

    for div in tmp:
        for link in div.find_all('a'):
            tags.add(link.text.strip())

    rating_div = soup.find('div',
                           attrs={'data-tippy-content':'Рейтинг'})
    rating = '?'
    if rating_div != None:
        rating = rating_div.text.strip()

    translation_quality_div = soup.find('div', attrs={'data-tippy-content':'Качество перевода'})
    translation_quality = '?'
    if translation_quality_div != None:
        translation_quality = translation_quality_div.text.strip().split()[0]
    
            
    print("name =", name)
    print('other names =', other_names)
    print("description =", description)
    print("tags =", tags)
    print('rating =', rating)
    print('translation_quality =', translation_quality)


def file_name(url):
    res = 'html/' + url.replace('https://', '').replace('/', '_') + '.html'
    #res = url.replace('https://', '').replace('/', '_') + '.html'
    print(res)
    return res

for i in range(1063, 1500):
    url = 'https://ranobehub.org/ranobe/' + str(i)
    print(url)
    response = request('GET', url)
    print(response.status_code)
    sleep(0.5)
    if response.status_code != 200:
        continue
    html_doc = response.text

    f = open(file_name(url), 'w', encoding="utf-8")
    f.write(html_doc)
    #f.write(html_doc.encode('utf8'))
    f.close()
    continue
    
    soup = BeautifulSoup (html_doc, 'html.parser')

    save_ranobe_page(soup)
    
    #print(soup.prettify())
    print('end for')
    #input()
