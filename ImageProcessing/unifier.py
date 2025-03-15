import os
from PIL import Image
import imagehash
import shutil
from pathlib import Path

test = False

imgs_directory = '../imgs/all/'
unique_directory = '../imgs/unique/'
similar_directory = '../imgs/similar/'
if test:
    imgs_directory = '../imgs/test/'
    unique_directory = '../imgs/test_unique/'
    similar_directory = '../imgs/test_similar/'

def clear_and_create_dir(dirname):
    try:
        shutil.rmtree(dirname)
    except:
        pass  
    Path(dirname).mkdir(parents=True, exist_ok=True)


clear_and_create_dir(unique_directory)
clear_and_create_dir(similar_directory)

unique_level = 10

def get_hash(image):
    return imagehash.phash(image)

hashes = []

cnt = 0
for fname in os.listdir(imgs_directory):
    path = imgs_directory + fname
    image = Image.open(path)
    phash = get_hash(image)
    #print(path, phash)
    hashes.append((path, phash))
    cnt += 1
    if cnt % 50 == 0:
        print('cnt =', cnt)

print('hashes have been calculated')

unique = []
similar = []
for i in range(len(hashes)):
    cur_path, cur_hash = hashes[i]
    flag = False
    for it in unique:
        i_path, i_hash = it
        dif = cur_hash - i_hash
        if dif < unique_level:
            flag = True
            similar.append((i_path, cur_path))

    if not flag:
        unique.append((cur_path, cur_hash))

    if i % 50 == 0:
        print('i =', i, 'of', len(hashes))

print('unique count:', len(unique))

def name_from_path(path, directory=imgs_directory):
    return path[len(imgs_directory):]

for i in range(min(len(similar), 10)):
    name_1, name_2 = map(name_from_path, similar[i])
    shutil.copy(similar[i][0], similar_directory + str(i) + '_1_' + name_1)
    shutil.copy(similar[i][1], similar_directory + str(i) + '_2_' + name_1)
    
for it in unique:
    path = it[0]
    name = path[len(imgs_directory):]
    shutil.copy(path, unique_directory + name)

    
