# Version v0.2 - On-device TFLite Prototype

## Overview

This version marks the transition from a client-server object recognition prototype to an Android application capable of executing object detection locally on the device.

The previous baseline version, tagged as `v0.1-client-server-baseline`, used the Android app as a client that sent images to a FastAPI backend. In version `v0.2-on-device-tflite`, the app includes a TensorFlow Lite model and can perform inference directly on Android.

After the initial implementation, additional evaluations were performed on an emulator, on a physical Android device, with real-world photos, and with controlled real-world photos.

## Version tag

`v0.2-on-device-tflite`

## Main objective

The main objective of this version was to validate whether a YOLO-based object detection model trained on the Objects in the Classroom dataset could be exported to TensorFlow Lite and integrated into the Android application for local inference.

## Main changes compared to v0.1

In version `v0.1-client-server-baseline`, the recognition pipeline was:

Android app -> FastAPI backend -> YOLO model -> JSON response -> app result -> Text-to-Speech

In version `v0.2-on-device-tflite`, the app also supports:

Android app -> local YOLO26n TFLite model -> app result -> Text-to-Speech

The backend mode was preserved for comparison and testing, but the main assistive flow now supports local inference.

## Implemented features

This version includes:

- YOLO26n TensorFlow Lite model added to Android assets;
- labels file added to Android assets;
- local TFLite model loading;
- image preprocessing for 640x640 input;
- local inference using TensorFlow Lite;
- post-processing of model output with shape `1 x 300 x 6`;
- class translation to Portuguese;
- confidence-based response messages;
- Text-to-Speech output;
- visual result display with detected object chips;
- backend mode preserved for comparison;
- functional evaluation with dataset images in emulator;
- functional evaluation with dataset images on physical device;
- evaluation with real-world photos;
- evaluation with controlled real-world photos;
- updated README with on-device status;
- version tag created and pushed to GitHub.

## Model used

`classroom_yolo26n_e50_best_float32.tflite`

Model characteristics:

- base architecture: YOLO26n
- format: TensorFlow Lite Float32
- input size: 640x640
- output shape observed in Android: `1 x 300 x 6`
- dataset: Objects in the Classroom
- number of classes: 20
- approximate model size: 9.47 MB

## Classes

The model was trained to recognize the following 20 classes:

- table
- chair
- whiteboard
- bookshelf
- clock
- wall-magazine
- trash-can
- eraser
- sharpener
- pen
- book
- ruler
- scissor
- fan
- laptop
- remote-control
- bag
- pants
- shoes
- hat

## Confidence handling

The app handles confidence levels differently:

- high confidence: the app states that the object was detected;
- medium confidence: the app reports a possible detection and suggests confirmation;
- low confidence or no valid detection: the app states that no object was recognized with safety.

This was added to avoid overconfident responses in uncertain cases, which is especially important in an assistive application.

## Evaluation 1 - Dataset images in emulator

A preliminary functional evaluation was performed using 20 images from the test split of the Objects in the Classroom dataset, with one image per class.

Summary:

- total images tested: 20
- correct detections: 17
- partial detections: 1
- errors: 1
- no safe detection: 1
- accuracy considering only correct detections: 85%
- accuracy considering correct + partial detections: 90%
- average approximate inference time: 368 ms
- minimum approximate inference time: 301 ms
- maximum approximate inference time: 713 ms

The first inference had the highest recorded time, which may be related to model initialization or warm-up in the emulator. Most subsequent tests were around 300 to 400 ms.

Detailed files:

- `docs/evaluation/on_device_functional_tests.csv`
- `docs/evaluation/on_device_functional_evaluation.md`

## Evaluation 2 - Dataset images on physical Android device

A second evaluation was performed on a physical Android device using 10 images from the same dataset-based evaluation.

Device:

- commercial model: Samsung S25 FE
- technical model: SM-S731B
- Android version: 16

Summary:

- total images tested: 10
- correct detections: 8
- partial detections: 0
- errors: 1
- no safe detection: 1
- accuracy considering only correct detections: 80%
- average approximate inference time: 167.3 ms
- minimum approximate inference time: 126 ms
- maximum approximate inference time: 207 ms

For the same 10 images, the average approximate time in the emulator was 382.5 ms. On the physical device, the average was 167.3 ms, approximately 2.3 times faster in this sample.

Detailed files:

- `docs/evaluation/physical_device_functional_tests.csv`
- `docs/evaluation/physical_device_functional_evaluation.md`

## Evaluation 3 - Real-world photos on physical device

A third evaluation used 20 real-world photos captured with the phone camera. These photos were taken in less controlled conditions, with natural background variation, different angles, distances, lighting, and object positions.

Summary:

- total real-world photos: 20
- objects inside the dataset classes: 16
- objects outside the dataset classes: 4
- average approximate inference time: 223.5 ms
- minimum approximate inference time: 157 ms
- maximum approximate inference time: 331 ms

For objects inside the dataset:

- correct detections: 5
- partial detections: 1
- errors: 4
- no safe detection: 6
- accuracy considering only correct detections: 31.25%
- accuracy considering correct + partial detections: 37.5%

For objects outside the dataset:

- no safe detection: 2
- moderate confusion: 2

This evaluation showed a significant drop in performance compared to dataset images. The result suggests a domain gap between the training/test dataset and real-world captured images.

Detailed files:

- `docs/evaluation/real_world_photo_tests.csv`
- `docs/evaluation/real_world_photo_evaluation.md`

## Evaluation 4 - Controlled real-world photos

A fourth evaluation used 15 real-world photos captured in more controlled conditions:

- clear background;
- better lighting;
- object centered;
- fewer objects in the scene;
- object occupying a relevant portion of the image.

Summary:

- total photos: 15
- objects inside the dataset classes: 11
- objects outside the dataset classes: 4
- average approximate inference time: 215.2 ms
- minimum approximate inference time: 98 ms
- maximum approximate inference time: 335 ms

For objects inside the dataset:

- correct detections: 8
- errors: 3
- no safe detection: 0
- accuracy considering only correct detections: 72.7%

For objects outside the dataset:

- no safe detection: 1
- high-confidence confusion: 1
- moderate confusion: 2

The controlled evaluation showed a clear improvement compared to the previous real-world evaluation. This indicates that capture conditions strongly affect model behavior.

Detailed files:

- `docs/evaluation/controlled_real_world_photo_tests.csv`
- `docs/evaluation/controlled_real_world_photo_evaluation.md`

## Consolidated interpretation

The on-device application is functional and can run object detection locally on Android.

The evaluations suggest four important conclusions:

1. The model performs well on dataset test images.
2. The model runs faster on a physical Android device than in the emulator.
3. Performance drops significantly on real-world photos captured in uncontrolled conditions.
4. Performance improves when real-world photos are captured with better lighting, clearer background, and centered objects.

This suggests that the model is technically integrated and efficient, but its robustness in real scenarios is still limited.

## Accessibility considerations

The evaluations showed that image capture quality has a strong impact on recognition quality. However, for the target audience of the project, especially blind users, the solution should not depend primarily on visual instructions such as written cards telling the user to centralize the object or improve the background.

For an assistive application, future improvements should prioritize accessible interaction mechanisms, such as:

- shorter and clearer Text-to-Speech messages;
- TalkBack-friendly labels;
- haptic feedback;
- guided capture through audio feedback;
- camera-based continuous or semi-continuous detection;
- avoiding overconfident spoken responses when the model is uncertain.

Visual guidance may be useful for development and testing, but it should not be treated as the main accessibility solution.

## Main observed successes

The model correctly recognized many classroom-related objects in dataset or controlled conditions, including:

- table;
- chair;
- whiteboard;
- bookshelf;
- clock;
- wall-magazine;
- pen;
- ruler;
- scissor;
- remote-control;
- sharpener;
- eraser;
- fan;
- laptop;
- bag in some cases;
- shoes;
- pants;
- hat in dataset images.

## Main observed limitations

The main limitations observed were:

- weak generalization to real-world photos;
- confusion between book/caderno covers and `wall-magazine`;
- confusion between hat/bag and shoes in some real photos;
- difficulty recognizing some small objects depending on angle and background;
- difficulty recognizing objects that are outside the 20 trained classes;
- sensitivity to capture conditions such as background, lighting, object size, and angle.

## Current status

The Android application is now a functional on-device object recognition prototype.

The project currently has two stable reference versions:

- `v0.1-client-server-baseline`
- `v0.2-on-device-tflite`

After the v0.2 tag, additional documentation was added to record evaluations on physical device and real-world photos.

## Next technical steps

The next steps before final reporting are:

1. improve accessible TTS feedback with shorter spoken messages;
2. improve TalkBack labels and content descriptions;
3. separate user-facing flow from testing/debug flow;
4. add haptic feedback for detection status;
5. compare YOLO26n TFLite with YOLOv8n TFLite inside the app;
6. test optimized exports, such as Float16;
7. compare model size, inference time, and qualitative behavior on Android;
8. improve image preprocessing, especially aspect ratio preservation or letterbox-style preprocessing;
9. investigate CameraX integration for continuous or semi-continuous detection;
10. keep final monograph/report writing as the last stage.