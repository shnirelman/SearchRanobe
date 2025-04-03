import os
from PIL import Image, ImageEnhance
import shutil
from pathlib import Path
import rembg
import cv2
import torch
from torchvision import models, transforms
import numpy as np

print('start process')

test = True

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

def remove_background_rembg_vanila(path, new_path):
    image = Image.open(path)
    enhancer = ImageEnhance.Contrast(image)
    image = enhancer.enhance(1.5)  # Увеличение контраста на 50%

    # Удаляем фон
    session = rembg.new_session("u2net")
    result = rembg.remove(image, session=session)
    #result = rembg.remove(image)
    result.save(new_path)

def remove_background_rembg(path, new_path):
    image = Image.open(path)
    enhancer = ImageEnhance.Contrast(image)
    image = enhancer.enhance(1.5)  # Увеличение контраста на 50%
    result = rembg.remove(image, session=session,
                        alpha_matting=True,
                        alpha_matting_foreground_threshold=270,
                        alpha_matting_background_threshold=20,
                        alpha_matting_erode_size=11)
    result.save(new_path)
    

def remove_background_cv2(path, new_path):
    image = cv2.imread(path)
    
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    _, mask = cv2.threshold(gray, 240, 255, cv2.THRESH_BINARY_INV)

    result = cv2.bitwise_and(image, image, mask=mask)
    cv2.imwrite(new_path, result)


model = models.segmentation.deeplabv3_resnet101(pretrained=True)
model.eval()
preprocess = transforms.Compose([
    transforms.Resize((256, 256)),
    transforms.ToTensor(),
    transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225]),
])

def remove_background_torch(path, new_path):
    image = Image.open(path)
    
    # Загрузка изображения
    input_image = Image.open(path).convert("RGB")  # Убедимся, что изображение в RGB
    original_size = input_image.size  # Сохраняем оригинальный размер изображения
    input_tensor = preprocess(input_image)
    input_batch = input_tensor.unsqueeze(0)

    # Прогноз
    with torch.no_grad():
        output = model(input_batch)['out'][0]
    output_predictions = output.argmax(0).byte().cpu().numpy()  # Преобразуем в маску

    # Изменяем размер маски до оригинального размера изображения
    mask = Image.fromarray(output_predictions).resize(original_size, Image.NEAREST)
    mask = np.array(mask)

    # Создаем прозрачный фон (альфа-канал)
    background = Image.new("RGBA", original_size, (255, 255, 255, 0))  # Прозрачный фон
    input_image = input_image.convert("RGBA")  # Преобразуем изображение в RGBA

    # Применяем маску к изображению
    result = Image.composite(input_image, background, Image.fromarray(mask).convert("L"))

    # Сохранение результата
    result.save(new_path)
    #result.save(new_path)

cnt = 0
for fname in os.listdir(imgs_directory):
    path = imgs_directory + fname
    new_path = without_back_directory + fname[:-3] + '.png'
    remove_background_rembg(path, new_path)

    cnt += 1
    if cnt % 50 == 0:
        print('cnt =', cnt)
