# Demo X-rays

Four wrist X-rays of children with a visible fracture, for the demo. Each one comes from the
**test** split of GRAZPEDWRI-DX (images the model was not trained on). On each of them the model finds
every fracture marked by the radiologists (IoU >= 0.5 with their box) and no other fracture.

`xrays/` has the images to upload or to show on a screen and photograph; `expected/` shows the box
the app should draw. Results through `POST /api/analyses` (contrast step off, the default):

| File | View | Fracture confidence | Box on the image (x, y, width, height) | GRAZPEDWRI-DX file |
|---|---|---|---|---|
| `1-right-front.png` | right wrist, front | 0.87 | 165, 642, 140, 147 | `4698_0614368684_01_WRI-R1_M015` |
| `2-left-front.png` | left wrist, front | 0.87 | 299, 714, 141, 100 | `6036_0730699241_01_WRI-L1_M010` |
| `3-left-side.png` | left wrist, side | 0.86 | 178, 433, 246, 133 | `0507_1135294951_01_WRI-L2_M015` |
| `4-right-side.png` | right wrist, side | 0.88 | 186, 543, 113, 77 | `0621_0497729112_01_WRI-R2_M014` |

The response also has a `text` box (the R / L marker on the film) and, for the side views,
`pronatorsign` (a soft-tissue sign that often goes with this fracture). The app can show only
`fracture` boxes if that is clearer for the demo.

## Running the demo locally
In Git Bash (Windows) or a terminal (Mac, Linux), from the repository folder:

```
# 1. the model, from the GitHub Release (models/ is not in git)
curl -L --create-dirs -o models/yolov8-fracture.onnx https://github.com/teq31/mobile-bone-fracture-detection/releases/download/model-v1/yolov8-fracture.onnx

# 2. the backend, with an administrator account (Windows: mvnw.cmd, Mac/Linux: sh mvnw)
ADMIN_EMAIL=admin@clinic.com ADMIN_PASSWORD=admin12345 sh mvnw spring-boot:run
```

In a second terminal, a patient account and one analysis:

```
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" \
     -d '{"name": "Ion Pop", "email": "ion@mail.com", "password": "patient123", "role": "PATIENT"}'
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" \
     -d '{"email": "ion@mail.com", "password": "patient123"}'
TOKEN=<the "token" from the login answer>
curl -X POST http://localhost:8080/api/analyses -H "Authorization: Bearer $TOKEN" \
     -F "image=@demo/xrays/1-right-front.png" -F "bodyPart=WRIST"
```

The answer has `"label":"fracture","confidence":0.87...` with the box `165, 642, 140, 147`, and the
analysis shows up in `GET /api/analyses/history`.

For the photo: show the X-ray full screen with high brightness, hold the phone parallel to the
screen and avoid reflections. The images here are 8-bit PNGs converted from the 16-bit originals.

Source: Nagy et al., "A pediatric wrist trauma X-ray dataset (GRAZPEDWRI-DX) for machine learning",
Scientific Data 9, 222 (2022), CC BY 4.0.
