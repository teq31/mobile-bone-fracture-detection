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

For the photo: show the X-ray full screen with high brightness, hold the phone parallel to the
screen and avoid reflections. The images here are 8-bit PNGs converted from the 16-bit originals.

Source: Nagy et al., "A pediatric wrist trauma X-ray dataset (GRAZPEDWRI-DX) for machine learning",
Scientific Data 9, 222 (2022), CC BY 4.0.
