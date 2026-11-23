# Analysis API contract

Draft contract for the analysis endpoints. Shared so the Flutter app (Teodora)
can work against mock data before the backend exists. Paths, fields and status
codes are a proposal and may change once the Spring Boot backend is in place.

Base path: `/api`

---

## POST /api/analyses

Upload one X-ray image and run a fracture analysis on it.

### Request

- Content-Type: `multipart/form-data`

| Field      | Type   | Required | Notes                                      |
|------------|--------|----------|--------------------------------------------|
| `image`    | file   | yes      | jpg / png / pdf / dicom                    |
| `bodyPart` | string | yes      | e.g. `WRIST`                               |
| `source`   | string | no       | `GALLERY` \| `CAMERA` \| `PDF` \| `DICOM`  |

### Response `201 Created`

```json
{
  "analysisId": "string",
  "model": "YOLO",
  "detections": [
    {
      "label": "fracture",
      "confidence": 0.87,
      "boundingBox": { "x": 0, "y": 0, "width": 0, "height": 0 },
      "heatmapUrl": "string"
    }
  ],
  "imageUrl": "string",
  "createdAt": "ISO-8601 timestamp"
}
```

### Errors

| Status | When                                       |
|--------|--------------------------------------------|
| 400    | missing/invalid image, unsupported type    |
| 413    | image larger than the configured max size  |
| 500    | analysis failed                            |

---

## GET /api/analyses/history

List past analyses for the current user.

### Response `200 OK`

```json
{
  "items": [
    {
      "analysisId": "string",
      "model": "YOLO",
      "imageUrl": "string",
      "createdAt": "ISO-8601 timestamp"
    }
  ]
}
```

---

## GET /api/analyses/{id}/comparison

Doctor only. Run every registered model on the same X-ray and return the
per-finding agreement (backed by `ModelComparisonService`).

### Response `200 OK`

```json
{
  "analysisId": "string",
  "totalModels": 2,
  "findings": [
    {
      "label": "fracture",
      "confidence": 0.81,
      "boundingBox": { "x": 0, "y": 0, "width": 0, "height": 0 },
      "agreementLevel": "FULL",
      "agreementCount": 2,
      "models": ["YOLO", "RF-DETR"]
    }
  ]
}
```

### Errors

| Status | When                                   |
|--------|----------------------------------------|
| 403    | caller is not a doctor                 |
| 404    | analysis id not found                  |
| 422    | fewer than 2 models registered         |
