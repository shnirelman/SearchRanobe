import os
from PIL import Image
import shutil
from pathlib import Path
import rembg

print('start process')

test = False

imgs_directory = '../imgs/unique/'
without_back_directory = '../imgs/without_back/'
if test:
    imgs_directory = '../imgs/test_unique/'
    without_back_directory = '../imgs/test_without_back/'

def clear_and_create_dir(dirname):
    try:
        shutil.rmtree(dirname)
    except:
        pass  
    Path(dirname).mkdir(parents=True, exist_ok=True)

clear_and_create_dir(without_back_directory)

model_name = "isnet-general-use"
session = rembg.new_session(model_name)

def remove_background(image):
    return rembg.remove(image, session=session,
                        alpha_matting=True,
                        alpha_matting_foreground_threshold=270,
                        alpha_matting_background_threshold=20,
                        alpha_matting_erode_size=11)

cnt = 0
for fname in os.listdir(imgs_directory):
    path = imgs_directory + fname
    image = Image.open(path)
    without_back = remove_background(image)
    newname = fname[:-3] + '.png'
    without_back.save(without_back_directory + newname)

    cnt += 1
    if cnt % 50 == 0:
        print('cnt =', cnt)
