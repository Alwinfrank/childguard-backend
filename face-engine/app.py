from __future__ import annotations

import os
from pathlib import Path
from typing import List, Optional
from urllib.parse import urlparse

import cv2
import numpy as np
import requests
from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI(title="ChildGuard Face Engine (CNN)")

EARTH_RADIUS_KM = 6371.0
MODEL_DIR = Path(__file__).resolve().parent / "models"
MODEL_DIR.mkdir(parents=True, exist_ok=True)

YUNET_MODEL = MODEL_DIR / "face_detection_yunet_2023mar.onnx"
SFACE_MODEL = MODEL_DIR / "face_recognition_sface_2021dec.onnx"

YUNET_URL = (
    "https://github.com/opencv/opencv_zoo/raw/main/models/face_detection_yunet/face_detection_yunet_2023mar.onnx"
)
SFACE_URL = (
    "https://github.com/opencv/opencv_zoo/raw/main/models/face_recognition_sface/face_recognition_sface_2021dec.onnx"
)


class FaceCandidate(BaseModel):
    id: int
    age: Optional[int] = None
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    photoUrl: Optional[str] = None


class FaceBatchMatchRequest(BaseModel):
    missingChildren: List[FaceCandidate]
    foundChildren: List[FaceCandidate]
    maxAgeDiff: int = 3
    maxDistanceKm: float = 5.0


class FaceMatchResult(BaseModel):
    missingId: int
    foundId: int
    similarity: float
    ageDifference: int
    distanceKm: float


def download_if_missing(path: Path, url: str) -> None:
    if path.exists():
        return
    resp = requests.get(url, timeout=60)
    resp.raise_for_status()
    path.write_bytes(resp.content)


def haversine(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    dlat = np.radians(lat2 - lat1)
    dlon = np.radians(lon2 - lon1)
    a = np.sin(dlat / 2) ** 2 + np.cos(np.radians(lat1)) * np.cos(np.radians(lat2)) * np.sin(dlon / 2) ** 2
    c = 2 * np.arctan2(np.sqrt(a), np.sqrt(1 - a))
    return float(EARTH_RADIUS_KM * c)


def read_image(photo_url: Optional[str]) -> Optional[np.ndarray]:
    if not photo_url:
        return None

    parsed = urlparse(photo_url)
    if parsed.scheme in ("http", "https"):
        resp = requests.get(photo_url, timeout=15)
        resp.raise_for_status()
        image_bytes = np.frombuffer(resp.content, dtype=np.uint8)
        return cv2.imdecode(image_bytes, cv2.IMREAD_COLOR)

    normalized = photo_url.replace("\\", "/")
    if not os.path.exists(normalized):
        project_root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
        candidate = os.path.join(project_root, normalized)
        if os.path.exists(candidate):
            normalized = candidate
        else:
            return None

    return cv2.imread(normalized)


def build_models():
    download_if_missing(YUNET_MODEL, YUNET_URL)
    download_if_missing(SFACE_MODEL, SFACE_URL)

    detector = cv2.FaceDetectorYN_create(
        str(YUNET_MODEL),
        "",
        (320, 320),
        score_threshold=0.8,
        nms_threshold=0.3,
        top_k=5000,
    )
    recognizer = cv2.FaceRecognizerSF_create(str(SFACE_MODEL), "")
    return detector, recognizer


DETECTOR, RECOGNIZER = build_models()


def extract_embedding(image_bgr: np.ndarray) -> Optional[np.ndarray]:
    if image_bgr is None:
        return None

    h, w = image_bgr.shape[:2]
    if h < 20 or w < 20:
        return None

    DETECTOR.setInputSize((w, h))
    _, faces = DETECTOR.detect(image_bgr)

    if faces is None or len(faces) == 0:
        return None

    # Pick highest-confidence face.
    best_face = max(faces, key=lambda f: float(f[-1]))
    aligned = RECOGNIZER.alignCrop(image_bgr, best_face)
    feature = RECOGNIZER.feature(aligned)
    return feature


def cosine_similarity(a: np.ndarray, b: np.ndarray) -> float:
    score = float(RECOGNIZER.match(a, b, cv2.FaceRecognizerSF_FR_COSINE))
    # Normalize to a clean 0..1 range for frontend confidence display.
    return max(0.0, min(1.0, score))


@app.get("/health")
def health() -> dict:
    return {"status": "ok", "engine": "opencv-cnn"}


@app.post("/match/batch", response_model=List[FaceMatchResult])
def match_batch(payload: FaceBatchMatchRequest) -> List[FaceMatchResult]:
    missing_embeddings = {}
    for child in payload.missingChildren:
        img = read_image(child.photoUrl)
        emb = extract_embedding(img) if img is not None else None
        if emb is not None:
            missing_embeddings[child.id] = emb

    found_embeddings = {}
    for child in payload.foundChildren:
        img = read_image(child.photoUrl)
        emb = extract_embedding(img) if img is not None else None
        if emb is not None:
            found_embeddings[child.id] = emb

    results: List[FaceMatchResult] = []
    for missing in payload.missingChildren:
        if missing.id not in missing_embeddings:
            continue
        if missing.age is None or missing.latitude is None or missing.longitude is None:
            continue

        for found in payload.foundChildren:
            if found.id not in found_embeddings:
                continue
            if found.age is None or found.latitude is None or found.longitude is None:
                continue

            age_diff = abs(missing.age - found.age)
            if age_diff >= payload.maxAgeDiff:
                continue

            distance_km = haversine(missing.latitude, missing.longitude, found.latitude, found.longitude)
            if distance_km >= payload.maxDistanceKm:
                continue

            similarity = cosine_similarity(missing_embeddings[missing.id], found_embeddings[found.id])
            results.append(
                FaceMatchResult(
                    missingId=missing.id,
                    foundId=found.id,
                    similarity=similarity,
                    ageDifference=age_diff,
                    distanceKm=distance_km,
                )
            )

    results.sort(key=lambda x: x.similarity, reverse=True)
    return results
