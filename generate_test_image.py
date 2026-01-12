from openai import OpenAI
import os
import base64

client = OpenAI()

# 使用 Gemini Imagen 生成测试图片
response = client.images.generate(
    model="imagen-3.0-generate-002",
    prompt="A serene mountain landscape at sunset with a lake in the foreground",
    n=1,
    size="1280x720"
)

# 保存图片
image_url = response.data[0].url
print(f"Generated image URL: {image_url}")

# 下载图片
import requests
img_data = requests.get(image_url).content
with open('test_image.jpg', 'wb') as f:
    f.write(img_data)
    
print("Test image saved to test_image.jpg")
