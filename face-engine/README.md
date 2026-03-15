# Face Engine (OpenCV + CNN)

This service provides face matching for ChildGuard.

## Run

```bash
cd face-engine
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
uvicorn app:app --host 0.0.0.0 --port 5000 --reload
```

## Endpoint

- `POST /match/batch`
- `GET /health`

Spring Boot uses `face.engine.url=http://localhost:5000/match/batch`.
