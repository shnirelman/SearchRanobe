import re

synonyms = dict()
links = dict()

f = open("synonym_dictionary.txt")

#p = re.compile('\')

re_goto = re.compile("[сС][мМ]\.")
re_syn = re.compile("\|\|")

def parse_list(s):
    a = s.split(',')
    res = []
    for x in a:
        x = x.strip()
            
        if x.find(' ') == -1:
            res.append(x)
    return res

def add_syn(word, syn):
    if word not in synonyms:
        synonyms[word] = []
    synonyms.append(syn)

def add_syns(word, syns):
    if word not in synonyms:
        synonyms[word] = []
    synonyms[word] += syns

def process_line(line):
    if not line:
        return
    
    line = line.replace('\n', ' ')
    line = re.sub('\[.*\]', '', line)
    line = re.sub('\(.*\)', '', line)
    line = re.sub('\|\|', '', line)
    
    #print(line)
    word = re.findall('^[а-яА-Я]+', line)[0]
    #print(word)

    ind = len(line)

    goto_pos = re_goto.finditer(line)
    for pos in goto_pos:
        #print(pos.start())
        ind = min(ind, pos.start())
        cur_link = line[pos.start() + 3:]
        links[word] = parse_list(cur_link)
        break

    dot_pos = line.find('.')
    if dot_pos != -1:
        ind = min(ind, dot_pos)

    words = parse_list(line[0:ind])
    synonyms[word] = words[1:]
    #for i in range(len(words)):
    #    add_syns(words
    #print("synonyms:", synonyms[word])
    '''
    syn_pos = re_syn.finditer(line)
    for pos in syn_pos:
        #print(pos.start())
        cur_syn = line[pos.start() + 3:]
        synonyms[word] = parse_list(cur_syn)
        print("synonyms:", synonyms[word])
        break
    '''

cur_line = ""
cnt = 0;
for line in f:
    if line[0].isalpha():
        process_line(cur_line)
        cur_line = line
        cnt += 1
        #if cnt > 1000:
        #    break
    else:
        cur_line += line
process_line(cur_line)

used = set()
def dfs(word):
    used.add(word)

    if word not in synonyms:
        synonyms[word] = []

    #print("dfs", word)
    
    if word not in links:
        return

    #print("dfs", word, links[word])
    
    for link in links[word]:
        if link not in used:
            dfs(link)
        synonyms[word] += synonyms[link]
        synonyms[word].append(link)

for word in links:
    if word not in used:
        dfs(word)

#print(synonyms)
x = 0
number = 0
file = open("synonyms.txt", "w")

for word, syns in synonyms.items():
    if syns:
        line = word + " : "
        for syn in syns:
            line += " " + syn
        file.write(line.lower() + "\n")
        
        
        number += len(syns)
        x += 1
        if x % 1000 == 0:
            print(word, syns)
print(number)

file.close()
