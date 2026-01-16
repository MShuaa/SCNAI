from flask import Flask, request, jsonify
from flask_cors import CORS
import torch
import torch.nn.functional as F
import torchvision.transforms as transforms
from PIL import Image
from efficientnet_pytorch import EfficientNet
from torch import nn
import io
import os

app = Flask(__name__)
CORS(app)  # 允许跨域

# 设备配置
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
print(f"使用设备: {device}")

# ----------------------------------------------------------
# 1. 模型结构（6 类）
# ----------------------------------------------------------
num_classes = 6
class_names = ['tanju', 'qingchong', 'shuangmei', 'jiankang', 'banqianying', 'zhenfeng']

model = EfficientNet.from_pretrained('efficientnet-b0')
in_features = model._fc.in_features
model._fc = nn.Linear(in_features, num_classes)
model.to(device)

# ----------------------------------------------------------
# 2. 加载权重
# ----------------------------------------------------------
model_path = 'models/best_model_g8lg426c.pth'
if not os.path.exists(model_path):
    print(f"错误: 模型文件不存在 - {model_path}")
    exit(1)

state_dict = torch.load(model_path, map_location=device)
model.load_state_dict(state_dict)
model.eval()
print("模型加载成功")

# ----------------------------------------------------------
# 3. 图像预处理
# ----------------------------------------------------------
transform = transforms.Compose([
    transforms.Resize(256),
    transforms.CenterCrop(224),
    transforms.ToTensor(),
    transforms.Normalize(mean=[0.485, 0.456, 0.406],
                         std=[0.229, 0.224, 0.225]),
])


# ----------------------------------------------------------
# 4. 核心识别函数
# ----------------------------------------------------------
def classify_image(image_data):
    """
    对输入的图像进行分类识别

    参数:
    image_data: PIL.Image 对象或图像数据

    返回:
    dict: 包含分类结果和置信度的字典
    """
    # 将输入转换为PIL图像
    if isinstance(image_data, Image.Image):
        pil_img = image_data
    else:
        pil_img = Image.open(io.BytesIO(image_data))

    # 转换为RGB格式（如果是RGBA或其他格式）
    if pil_img.mode != 'RGB':
        pil_img = pil_img.convert('RGB')

    # 应用预处理转换
    tensor = transform(pil_img).unsqueeze(0).to(device)

    # 推理
    with torch.no_grad():
        logits = model(tensor)
        probs = F.softmax(logits[0], dim=0)
        top_prob, top_idx = torch.topk(probs, 1)

        label = class_names[top_idx[0].item()]
        prob = top_prob[0].item()
        all_probs = probs.tolist()

    # 构建详细结果
    result = {
        "label": label,
        "confidence": prob,
        "all_predictions": [
            {"class": class_names[i], "confidence": all_probs[i]}
            for i in range(num_classes)
        ]
    }

    return result


# ----------------------------------------------------------
# 5. Flask API端点
# ----------------------------------------------------------
@app.route('/predict', methods=['POST'])
def predict():
    """
    图像识别API端点
    接收POST请求,包含上传的图像文件
    """
    try:
        # 检查是否有文件
        if 'file' not in request.files:
            return jsonify({
                'success': False,
                'error': '没有上传文件'
            }), 400

        file = request.files['file']

        # 检查文件名
        if file.filename == '':
            return jsonify({
                'success': False,
                'error': '文件名为空'
            }), 400

        # 读取图像
        image_bytes = file.read()

        # 进行分类
        result = classify_image(image_bytes)

        # 返回成功结果
        return jsonify({
            'success': True,
            **result
        })

    except Exception as e:
        print(f"预测错误: {str(e)}")
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500


@app.route('/health', methods=['GET'])
def health():
    """健康检查端点"""
    return jsonify({
        'status': 'ok',
        'model': 'EfficientNet-B0',
        'classes': class_names,
        'device': str(device)
    })


@app.route('/', methods=['GET'])
def index():
    """主页"""
    return jsonify({
        'message': 'SCNAI植物病虫害AI识别服务',
        'version': '1.0.0',
        'endpoints': {
            '/predict': 'POST - 图像识别',
            '/health': 'GET - 健康检查'
        }
    })


# ----------------------------------------------------------
# 6. 启动服务
# ----------------------------------------------------------
if __name__ == "__main__":
    print("=" * 60)
    print("SCNAI植物病虫害AI识别服务")
    print("=" * 60)
    print(f"模型: EfficientNet-B0")
    print(f"类别: {', '.join(class_names)}")
    print(f"设备: {device}")
    print("=" * 60)
    print("服务启动在: http://localhost:5001")
    print("=" * 60)

    app.run(host='0.0.0.0', port=5001, debug=False)
