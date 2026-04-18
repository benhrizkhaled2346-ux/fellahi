import warnings
warnings.filterwarnings("ignore")

import os
import sys
import json
import torch
import torch.nn as nn
from torchvision.models import mobilenet_v3_small
from torchvision import transforms
from PIL import Image

# =======================
# CONFIG
# =======================

CLASSES = [
    "olive_aculus_olearius",
    "olive_healthy",
    "olive_peacock_spot",
    "palm_black_scorch",
    "palm_fusarium_wilt",
    "palm_healthy",
    "palm_leaf_spots",
    "palm_magnesium_deficiency",
    "palm_manganese_deficiency",
    "palm_parlatoria_blanchardi",
    "palm_potassium_deficiency",
    "palm_rachis_blight"
]

CONFIDENCE_THRESHOLD = 0.60

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_PATH = os.path.join(BASE_DIR, "student_best.pth")

device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

# =======================
# MODEL
# =======================

def load_model():
    model = mobilenet_v3_small(weights=None)

    model.classifier[3] = nn.Linear(
        model.classifier[3].in_features,
        len(CLASSES)
    )

    state_dict = torch.load(
        MODEL_PATH,
        map_location=device,
        weights_only=True
    )

    model.load_state_dict(state_dict)
    model.to(device)
    model.eval()
    return model


model = load_model()  # ✅ LOAD ONCE (IMPORTANT)

# =======================
# TRANSFORM
# =======================

transform = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor(),
    transforms.Normalize(
        [0.485, 0.456, 0.406],
        [0.229, 0.224, 0.225]
    ),
])

# =======================
# PREDICTION
# =======================

def predict(image_path):
    image = Image.open(image_path).convert("RGB")
    tensor = transform(image).unsqueeze(0).to(device)

    with torch.no_grad():
        output = model(tensor)

    probs = torch.softmax(output, dim=1)[0]

    top_prob = float(probs.max())
    top_class = int(probs.argmax())

    # Not a leaf case
    if top_prob < CONFIDENCE_THRESHOLD:
        return {
            "valid": False,
            "message": "Not a leaf"
        }

    top3 = sorted(
        [
            {"class": CLASSES[i], "prob": float(probs[i])}
            for i in range(len(CLASSES))
        ],
        key=lambda x: x["prob"],
        reverse=True
    )[:3]

    return {
        "valid": True,
        "disease": CLASSES[top_class],
        "confidence": round(top_prob * 100, 2),
        "top3": top3
    }

# =======================
# MAIN (Spring Boot CALL)
# =======================

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(json.dumps({
            "valid": False,
            "message": "No image path provided"
        }))
        sys.exit(1)

    image_path = sys.argv[1]

    result = predict(image_path)

    # IMPORTANT: ONLY OUTPUT JSON
    print(json.dumps(result))