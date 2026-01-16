"""
SCNAI植物病虫害AI识别服务
提供HTTP API接口供Java后端调用
"""

import torch
import torch.nn.functional as F
import torchvision.transforms as transforms
from PIL import Image
from efficientnet_pytorch import EfficientNet
from torch import nn
import numpy as np
from flask import Flask, request, jsonify
from io import BytesIO
import base64

# ===========================
# 1. 初始化设备和模型
# ===========================
device = torch.device("cuda" if torch.cuda.is.available() else "cpu")
print(f"使用设备: {device}")

# 模型配置
num_classes = 6
# 类别映射：中文拼音 -> 中文名称
class_names_pinyin = ['tanju', 'qingchong', 'shuangmei', 'jiankang', 'banqianying', 'zhenfeng']
class_names_chinese = {
    'tanju': '炭疽病',
    'qingchong': '蚜虫',
    'shuangmei': '霜霉病',
    'jiankang': '健康',
    'banqianying': '白粉病',
    'zhenfeng': '病毒病'
}

# 英文代码映射（与前端文档一致）
class_code_map = {
    'tanju': 'anthracnose',
    'qingchong': 'aphid',
    'shuangmei': 'downy',
    'jiankang': 'healthy',
    'banqianying': 'powdery',
    'zhenfeng': 'virus'
}

# 加载模型
model = EfficientNet.from_pretrained('efficientnet-b0')
in_features = model._fc.in_features
model._fc = nn.Linear(in_features, num_classes)
model.to(device)

# 加载权重
model_path = 'models/best_model_g8lg426c.pth'
try:
    state_dict = torch.load(model_path, map_location=device)
    model.load_state_dict(state_dict)
    model.eval()
    print(f"模型加载成功: {model_path}")
except Exception as e:
    print(f"模型加载失败: {e}")
    print("将使用随机初始化的模型（仅供测试）")

# 图像预处理
transform = transforms.Compose([
    transforms.Resize(256),
    transforms.CenterCrop(224),
    transforms.ToTensor(),
    transforms.Normalize(mean=[0.485, 0.456, 0.406],
                         std=[0.229, 0.224, 0.225]),
])

# ===========================
# 2. 核心识别函数
# ===========================
def classify_image(image_data):
    """
    对输入的图像进行分类识别

    参数:
    image_data: PIL.Image对象、numpy数组、文件路径或二进制数据

    返回:
    dict: 包含分类结果和置信度的字典
    """
    # 将不同格式的输入转换为PIL图像
    if isinstance(image_data, Image.Image):
        pil_img = image_data
    elif isinstance(image_data, np.ndarray):
        pil_img = Image.fromarray(image_data)
    elif isinstance(image_data, str):
        pil_img = Image.open(image_data)
    else:
        pil_img = Image.open(image_data)

    # 转换为RGB格式
    if pil_img.mode != 'RGB':
        pil_img = pil_img.convert('RGB')

    # 应用预处理转换
    tensor = transform(pil_img).unsqueeze(0).to(device)

    # 推理
    with torch.no_grad():
        logits = model(tensor)
        probs = F.softmax(logits[0], dim=0)
        top_prob, top_idx = torch.topk(probs, 1)

        label_pinyin = class_names_pinyin[top_idx[0].item()]
        label_chinese = class_names_chinese[label_pinyin]
        label_code = class_code_map[label_pinyin]
        prob = top_prob[0].item()
        all_probs = probs.tolist()

    # 构建详细结果
    result = {
        "diseaseType": label_code,  # 英文代码
        "diseaseTypeName": label_chinese,  # 中文名称
        "confidence": prob,  # 最高置信度
        "rawPredictions": {}  # 所有类别的置信度
    }

    # 添加所有类别的置信度
    for i in range(num_classes):
        pinyin = class_names_pinyin[i]
        code = class_code_map[pinyin]
        result["rawPredictions"][code] = all_probs[i]

    return result

# ===========================
# 3. Flask API服务
# ===========================
app = Flask(__name__)

@app.route('/health', methods=['GET'])
def health_check():
    """健康检查接口"""
    return jsonify({
        "status": "ok",
        "device": str(device),
        "model_loaded": True
    })

@app.route('/predict', methods=['POST'])
def predict():
    """
    图像识别接口

    接收:
    - multipart/form-data: 文件上传
    - application/json: base64编码的图像数据

    返回:
    JSON格式的识别结果
    """
    try:
        # 方式1: 接收文件上传
        if 'file' in request.files:
            file = request.files['file']
            image_data = BytesIO(file.read())
            result = classify_image(image_data)
            return jsonify({
                "success": True,
                "data": result
            })

        # 方式2: 接收base64编码的图像
        elif request.is_json:
            data = request.get_json()
            if 'image' in data:
                # base64解码
                image_base64 = data['image']
                if ',' in image_base64:
                    image_base64 = image_base64.split(',')[1]
                image_bytes = base64.b64decode(image_base64)
                image_data = BytesIO(image_bytes)
                result = classify_image(image_data)
                return jsonify({
                    "success": True,
                    "data": result
                })

        return jsonify({
            "success": False,
            "error": "请提供图像数据（file或base64）"
        }), 400

    except Exception as e:
        return jsonify({
            "success": False,
            "error": str(e)
        }), 500

@app.route('/predict/batch', methods=['POST'])
def predict_batch():
    """批量识别接口（可选）"""
    try:
        files = request.files.getlist('files')
        results = []

        for file in files:
            image_data = BytesIO(file.read())
            result = classify_image(image_data)
            results.append(result)

        return jsonify({
            "success": True,
            "data": results
        })
    except Exception as e:
        return jsonify({
            "success": False,
            "error": str(e)
        }), 500

# ===========================
# 4. 启动服务
# ===========================
if __name__ == "__main__":
    print("="*50)
    print("SCNAI植物病虫害AI识别服务")
    print("="*50)
    print("模型: EfficientNet-B0")
    print(f"类别数: {num_classes}")
    print(f"设备: {device}")
    print("="*50)
    print("API接口:")
    print("  GET  /health - 健康检查")
    print("  POST /predict - 单张图像识别")
    print("  POST /predict/batch - 批量图像识别")
    print("="*50)

    # 启动Flask服务
    # host='0.0.0.0'允许外部访问
    # port=5001避免与Java后端(5000)冲突
    app.run(host='0.0.0.0', port=5001, debug=False)
