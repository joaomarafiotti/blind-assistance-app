\# Version v0.2 - On-device TFLite Prototype



\## Overview



This version marks the transition from a client-server object recognition prototype to an Android application capable of executing object detection locally on the device.



The previous baseline version, tagged as `v0.1-client-server-baseline`, used the Android app as a client that sent images to a FastAPI backend. In version `v0.2-on-device-tflite`, the app includes a TensorFlow Lite model and can perform inference directly on Android.



\## Version tag



```text

v0.2-on-device-tflite

````



\## Main objective



The main objective of this version was to validate whether a YOLO-based object detection model trained on the Objects in the Classroom dataset could be exported to TensorFlow Lite and integrated into the Android application for local inference.



\## Main changes compared to v0.1



In version `v0.1-client-server-baseline`, the recognition pipeline was:



```text

Android app → FastAPI backend → YOLO model → JSON response → app result → Text-to-Speech

```



In version `v0.2-on-device-tflite`, the app also supports:



```text

Android app → local YOLO26n TFLite model → app result → Text-to-Speech

```



The backend mode was preserved for comparison, but the main assistive flow now supports local inference.



\## Implemented features



This version includes:



\* YOLO26n TensorFlow Lite model added to Android assets;

\* labels file added to Android assets;

\* local TFLite model loading;

\* image preprocessing for 640x640 input;

\* local inference using TensorFlow Lite;

\* post-processing of model output with shape `1 x 300 x 6`;

\* class translation to Portuguese;

\* confidence-based response messages;

\* Text-to-Speech output;

\* visual result display with detected object chips;

\* functional evaluation with 20 test images;

\* updated README with on-device status;

\* version tag created and pushed to GitHub.



\## Model used



```text

classroom\_yolo26n\_e50\_best\_float32.tflite

```



Model characteristics:



\* base architecture: YOLO26n

\* format: TensorFlow Lite Float32

\* input size: 640x640

\* output shape observed in Android: 1 x 300 x 6

\* dataset: Objects in the Classroom

\* number of classes: 20

\* approximate model size: 9.47 MB



\## Classes



The model was trained to recognize the following 20 classes:



```text

table

chair

whiteboard

bookshelf

clock

wall-magazine

trash-can

eraser

sharpener

pen

book

ruler

scissor

fan

laptop

remote-control

bag

pants

shoes

hat

```



\## Confidence handling



The app now handles confidence levels differently:



\* high confidence: the app states that the object was detected;

\* medium confidence: the app reports a possible detection and suggests confirmation;

\* low confidence or no valid detection: the app states that no object was recognized with safety.



This was added to avoid overconfident responses in uncertain cases, which is especially important in an assistive application.



\## Functional evaluation



A preliminary functional evaluation was performed using 20 images from the test split of the Objects in the Classroom dataset, with one image per class.



Summary:



```text

Total images tested: 20

Correct detections: 17

Partial detections: 1

Errors: 1

No safe detection: 1



Accuracy considering only correct detections: 85%

Accuracy considering correct + partial detections: 90%



Average approximate inference time: 368 ms

Minimum approximate inference time: 301 ms

Maximum approximate inference time: 713 ms

```



The first inference had the highest recorded time, which may be related to model initialization or warm-up in the emulator. Most subsequent tests were around 300 to 400 ms.



\## Main observed successes



The model correctly recognized most classroom-related objects, including:



\* table;

\* chair;

\* whiteboard;

\* bookshelf;

\* clock;

\* wall-magazine;

\* pen;

\* book;

\* ruler;

\* scissor;

\* fan;

\* laptop;

\* remote-control;

\* bag;

\* pants;

\* shoes;

\* hat.



\## Main observed limitations



The main limitations observed in the functional evaluation were:



\* `trash-can` was not recognized with safety;

\* `eraser` was confused with `sharpener`;

\* `sharpener` was detected correctly, but also produced an additional `eraser` detection.



A qualitative test with an online pencil image also produced a possible `ruler` detection. This is expected because pencil is not part of the 20 classes of the dataset, showing that objects outside the trained class set may be confused with visually similar classes.



\## Current status



The Android application is now a functional on-device object recognition prototype.



The project currently has two stable reference versions:



```text

v0.1-client-server-baseline

v0.2-on-device-tflite

```



\## Next technical steps



The next steps before final reporting are:



1\. run tests on a physical Android device;

2\. test real-world photos captured outside the dataset;

3\. improve accessibility and user experience;

4\. compare YOLO26n TFLite with YOLOv8n TFLite inside the app;

5\. test optimized exports, such as Float16;

6\. compare mobile inference time, size, and qualitative behavior;

7\. investigate CameraX integration for near real-time or continuous detection;

8\. keep final monograph/report writing as the last stage.

