import io
import os
import glob
import cv2
import time
import jwt
import torch
import requests
import logging
import threading
import numpy as np
import torch.nn as nn
from PIL import Image
import torchvision.models as models
import torchvision.transforms as transforms
from flask import Flask, Response, request
from flask_cors import CORS
from jwt.exceptions import ExpiredSignatureError, InvalidTokenError
from ultralytics import YOLO

########################################
# FLASK APP SETUP
########################################
app = Flask(__name__)

CORS(app, resources={r"/*": {"origins": "http://localhost:3000"}})

SECRET_KEY = 'very_long_and_super_secure_secret_key'

BACKEND_LOG_URL = 'http://localhost:8080/api/logs'

########################################
# GLOBAL DICTIONARY TO HOLD USER/CAMERA STATE
########################################
# user_detection_state[user_id] = {
#     "camera_url": ...,
#     "detect_dog": bool,
#     "dog_ref_embedding": np.array(...) or None,
#     "dog_state": "outside"/"inside"/None,
#     "pet_id": int or None,
#     "latest_frame": b'...',
#     "thread": threading.Thread,
#     "stop_signal": bool
# }
user_detection_state = {}

########################################
# TOKEN VALIDATION
########################################
def validate_token(token):
    try:
        decoded = jwt.decode(token, SECRET_KEY, algorithms=['HS256'])
        return decoded
    except ExpiredSignatureError:
        print("Token has expired")
        return None
    except InvalidTokenError as e:
        print(f"Invalid token: {e}")
        return None

########################################
# EMBEDDING MODEL & UTILITIES
########################################
def create_embedding_model():
    model = models.resnet18(pretrained=True)
    model = nn.Sequential(*list(model.children())[:-1])
    model.eval()
    return model

@torch.no_grad()
def get_embedding(model, img_pil):
    transform = transforms.Compose([
        transforms.Resize((224, 224)),
        transforms.ToTensor(),
        transforms.Normalize(
            mean=[0.485, 0.456, 0.406],
            std=[0.229, 0.224, 0.225]
        )
    ])
    img_t = transform(img_pil).unsqueeze(0)
    features = model(img_t)
    features = features.view(features.size(0), -1)
    return features[0].cpu().numpy()

def compute_dog_reference_embedding(embedding_model, ref_image_dir):
    ref_paths = glob.glob(os.path.join(ref_image_dir, '*.png')) + \
                glob.glob(os.path.join(ref_image_dir, '*.jpg'))
    if not ref_paths:
        raise ValueError(f"No reference images found in {ref_image_dir}!")

    embeddings = []
    for path in ref_paths:
        img_pil = Image.open(path).convert('RGB')
        emb = get_embedding(embedding_model, img_pil)
        embeddings.append(emb)

    dog_ref_mean = np.mean(embeddings, axis=0)
    return dog_ref_mean

def cosine_similarity(vecA, vecB):
    dot = np.dot(vecA, vecB)
    normA = np.linalg.norm(vecA)
    normB = np.linalg.norm(vecB)
    return dot / (normA * normB + 1e-8)

def is_inside_polygon(point, polygon_pts):
    pt_array = np.array([polygon_pts], dtype=np.int32)
    result = cv2.pointPolygonTest(pt_array, point, False)
    return (result >= 0)

def log_dog_state_change(pet_id, previous_state, current_state):
    payload = {
        "previousState": previous_state,
        "currentState": current_state,
        "timestamp": int(time.time() * 1000),
        "petId": pet_id
    }
    try:
        response = requests.post(
            BACKEND_LOG_URL,
            json=payload,
            headers={"Content-Type": "application/json"}
        )
        if response.status_code == 200:
            print("State change logged successfully")
        else:
            print(f"Failed to log state change: {response.status_code}, {response.text}")
    except requests.RequestException as e:
        print(f"Exception during state change log: {e}")

########################################
# BACKGROUND THREAD
########################################
def background_camera_loop(user_id):
    """
    Continuously grabs frames from the user's camera.
    If detect_dog == True and dog_ref_embedding is not None,
    runs YOLO + ReID + logs state changes.
    Otherwise, just passes frames through unmodified.
    """
    global user_detection_state

    yolo_model = YOLO('yolov8n.pt')
    embed_model = create_embedding_model()

    door_polygon = [(1700, 800), (1920, 200), (1600, 50), (1500, 570)]
    threshold = 0.8

    while not user_detection_state[user_id]["stop_signal"]:
        camera_url = user_detection_state[user_id]["camera_url"]
        print(f"[{user_id}] Opening camera: {camera_url}")
        cap = cv2.VideoCapture(camera_url)

        if not cap.isOpened():
            print(f"[{user_id}] Failed to open camera. Retrying in 5s.")
            time.sleep(5)
            continue

        while cap.isOpened() and not user_detection_state[user_id]["stop_signal"]:
            ret, frame = cap.read()
            if not ret:
                print(f"[{user_id}] Failed to read frame. Reinit camera in 5s.")
                cap.release()
                time.sleep(5)
                break

            detect_dog = user_detection_state[user_id]["detect_dog"]
            dog_ref_emb = user_detection_state[user_id]["dog_ref_embedding"]
            dog_state = user_detection_state[user_id]["dog_state"]
            pet_id = user_detection_state[user_id]["pet_id"]

            annotated_frame = frame.copy()

            if detect_dog and dog_ref_emb is not None:
                results = yolo_model(frame)
                annotated_frame = results[0].plot()

                best_sim = -1
                best_box = None

                for box in results[0].boxes:
                    x1, y1, x2, y2 = box.xyxy[0].tolist()
                    x1, y1, x2, y2 = int(x1), int(y1), int(x2), int(y2)

                    obj_crop_bgr = frame[y1:y2, x1:x2]
                    if obj_crop_bgr.size == 0:
                        continue

                    obj_crop_rgb = cv2.cvtColor(obj_crop_bgr, cv2.COLOR_BGR2RGB)
                    obj_crop_pil = Image.fromarray(obj_crop_rgb)

                    test_emb = get_embedding(embed_model, obj_crop_pil)
                    sim = cosine_similarity(test_emb, dog_ref_emb)
                    if sim > best_sim:
                        best_sim = sim
                        best_box = (x1, y1, x2, y2)

                previous_state = dog_state
                if best_box is not None and best_sim >= threshold:
                    (x1, y1, x2, y2) = best_box
                    cv2.rectangle(annotated_frame, (x1, y1),
                                  (x2, y2), (0, 255, 0), 2)
                    label = f"My Dog ({best_sim:.2f})"
                    cv2.putText(annotated_frame, label, (x1, y1 - 5),
                                cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 0), 2)

                    center_x = (x1 + x2) // 2
                    center_y = (y1 + y2) // 2
                    cv2.circle(annotated_frame, (center_x, center_y),
                               5, (0,255,0), -1)

                    inside_poly = is_inside_polygon((center_x, center_y),
                                                    door_polygon)

                    if dog_state == "outside" and inside_poly:
                        dog_state = "inside"
                        log_dog_state_change(pet_id, previous_state, dog_state)
                    elif dog_state == "inside" and not inside_poly:
                        dog_state = "outside"
                        log_dog_state_change(pet_id, previous_state, dog_state)

                poly_pts = np.array([door_polygon], dtype=np.int32)
                cv2.polylines(annotated_frame, poly_pts, isClosed=True,
                              color=(255, 0, 0), thickness=2)

                cv2.putText(annotated_frame, f"Dog State: {dog_state}",
                            (50, 50), cv2.FONT_HERSHEY_SIMPLEX,
                            1.0, (0, 255, 0), 2)

                user_detection_state[user_id]["dog_state"] = dog_state

            ret2, buffer = cv2.imencode('.jpg', annotated_frame)
            user_detection_state[user_id]["latest_frame"] = buffer.tobytes()

        if cap is not None:
            cap.release()

    print(f"[{user_id}] Background thread stopped.")


########################################
# ROUTES
########################################

@app.route("/setup_camera", methods=["POST"])
def setup_camera():
    """
    JSON body:
    {
      "token": "...",
      "userId": "123",
      "cameraUrl": "rtsp://...",
    }
    """
    data = request.json
    token = data.get("token")
    user_id = data.get("userId")
    camera_url = data.get("cameraUrl")

    decoded = validate_token(token)
    if not decoded:
        return {"message": "Invalid or expired token"}, 401

    if user_id in user_detection_state:
        user_detection_state[user_id]["stop_signal"] = True
        old_thread = user_detection_state[user_id]["thread"]
        if old_thread and old_thread.is_alive():
            old_thread.join(timeout=5)
        del user_detection_state[user_id]

    user_detection_state[user_id] = {
        "camera_url": camera_url,
        "detect_dog": False,
        "dog_ref_embedding": None,
        "dog_state": None,
        "pet_id": None,
        "latest_frame": None,
        "thread": None,
        "stop_signal": False
    }

    t = threading.Thread(target=background_camera_loop,
                         args=(user_id,),
                         daemon=True)
    user_detection_state[user_id]["thread"] = t
    t.start()

    return {"message": f"Camera set up for user {user_id}"}, 200


@app.route("/add_pet", methods=["POST"])
def add_pet():
    """
    JSON body:
    {
      "token": "my-super-secret-service-token",
      "userId": "123",
      "petId": 999,
      "refImageUrls": ["https://s3.../pet1.jpg", "https://s3.../pet2.jpg"]
    }
    """
    data = request.json
    token = data.get("token")
    user_id = data.get("userId")
    pet_id = data.get("petId")
    ref_image_urls = data.get("refImageUrls", [])

    if token != "my-super-secret-service-token":
        return {"message": "Invalid token"}, 401

    if user_id not in user_detection_state:
        return {"message": f"No camera thread running for user {user_id}"}, 400

    local_dir = os.path.join("reference_images", f"user_{user_id}", f"pet_{pet_id}")
    os.makedirs(local_dir, exist_ok=True)

    downloaded_paths = []
    for i, url in enumerate(ref_image_urls):
        filename = f"ref_{i}.jpg"
        local_path = os.path.join(local_dir, filename)
        try:
            resp = requests.get(url)
            if resp.status_code == 200:
                with open(local_path, "wb") as f:
                    f.write(resp.content)
                downloaded_paths.append(local_path)
            else:
                print(f"Failed to download {url}: status {resp.status_code}")
        except Exception as e:
            print(f"Error downloading {url}: {e}")

    if not downloaded_paths:
        return {"message": "No valid images downloaded"}, 400

    embedding_model = create_embedding_model()
    embeddings = []
    for path in downloaded_paths:
        img_pil = Image.open(path).convert('RGB')
        emb = get_embedding(embedding_model, img_pil)
        embeddings.append(emb)

    dog_ref_mean = np.mean(embeddings, axis=0)

    user_detection_state[user_id]["detect_dog"] = True
    user_detection_state[user_id]["dog_ref_embedding"] = dog_ref_mean
    user_detection_state[user_id]["dog_state"] = "outside"
    user_detection_state[user_id]["pet_id"] = pet_id

    return {"message": f"Pet {pet_id} added. Downloaded {len(downloaded_paths)} images."}, 200

@app.route("/stop_camera", methods=["POST"])
def stop_camera():
    """
    JSON body:
    {
      "token": "...",
      "userId": "123"
    }
    Gracefully stop background thread for this user.
    """
    data = request.json
    token = data.get("token")
    user_id = data.get("userId")

    decoded = validate_token(token)
    if not decoded:
        return {"message": "Invalid or expired token"}, 401

    if user_id not in user_detection_state:
        return {"message": "No camera running for this user"}, 400

    user_detection_state[user_id]["stop_signal"] = True
    t = user_detection_state[user_id]["thread"]
    if t and t.is_alive():
        t.join(timeout=5)

    del user_detection_state[user_id]
    return {"message": f"Camera stopped for user {user_id}"}, 200


@app.route('/video_feed')
def video_feed():
    """
    Query params:
      token=...
      userId=...
    Returns the latest annotated (or raw) frame in MJPEG stream.
    """
    token = request.args.get('token')
    user_id = request.args.get('userId')

    decoded = validate_token(token)
    if not decoded:
        return Response('Invalid or expired token', status=401)

    if user_id not in user_detection_state:
        return Response('No camera running for this user', status=400)

    def gen_frames():
        while True:
            if user_id not in user_detection_state:
                break
            frame = user_detection_state[user_id].get("latest_frame", None)
            if frame is not None:
                yield (b'--frame\r\n'
                       b'Content-Type: image/jpeg\r\n\r\n' +
                       frame + b'\r\n')
            else:
                time.sleep(0.1)

    return Response(gen_frames(),
                    mimetype='multipart/x-mixed-replace; boundary=frame')


########################################
# MAIN
########################################
if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5001, debug=True)
