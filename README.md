# Blind Assistance App

Anonymized Android prototype for assistive object recognition using on-device object detection.

This repository contains the source code of an Android application developed for research on assistive object recognition. The prototype recognizes common objects in indoor environments and presents the result through text, speech, vibration, and a semi-continuous camera-based detection mode.

Author, institution, and repository owner information have been omitted for double-anonymous review.

## Goal

The goal of this prototype is to investigate mobile object recognition for assistive use, especially for blind or visually impaired users.

The project prioritizes:

* on-device inference;
* reduced dependency on network connectivity;
* lower latency compared with a remote-only architecture;
* privacy, since images do not need to be sent to a server in the on-device mode;
* accessible feedback through speech and vibration;
* compatibility with Android mobile devices.

## Project overview

The project started with a client-server architecture. In that version, the Android app captured or selected an image and sent it to a FastAPI backend, which executed a YOLO model and returned detections as JSON.

The project later evolved to on-device inference using TensorFlow Lite. The final Android prototype keeps the server-based mode as a baseline, but the main implementation uses local inference and a semi-continuous CameraX mode.

## Technologies

* Kotlin
* Android Studio
* Jetpack Compose
* CameraX
* TensorFlow Lite
* Text-to-Speech
* Vibration / haptic feedback
* TalkBack semantics
* OkHttp
* YOLO / Ultralytics
* FastAPI backend used as baseline

## Architecture

### Client-server baseline

```text
captured or selected image
        |
Android app
        |
FastAPI backend
        |
YOLO model on the server
        |
JSON response
        |
result in the app + Text-to-Speech
```

### On-device inference

```text
captured or selected image
        |
letterbox preprocessing
        |
local YOLO TFLite model
        |
post-processing
        |
visual result + speech + vibration
```

### Semi-continuous CameraX detection

```text
CameraX preview
        |
periodic frame capture
        |
frame conversion to Bitmap
        |
letterbox preprocessing
        |
on-device TFLite inference
        |
visual overlay
        |
Text-to-Speech
        |
vibration
        |
cooldown to avoid excessive repetition
```

## Available models

The app includes three TensorFlow Lite models in the assets folder:

| Model           | File                                        | Use                      |
| --------------- | ------------------------------------------- | ------------------------ |
| YOLO26n Float32 | `classroom_yolo26n_e50_best_float32.tflite` | default model            |
| YOLO26n Float16 | `classroom_yolo26n_e50_best_float16.tflite` | experimental alternative |
| YOLOv8n Float32 | `classroom_yolov8n_e50_best_float32.tflite` | comparative reference    |

The final default model is YOLO26n Float32.

## Dataset

The models were fine-tuned using the Objects in the Classroom dataset, which contains 20 indoor and educational object classes:

```text
table, chair, whiteboard, bookshelf, clock, wall-magazine, trash-can,
eraser, sharpener, pen, book, ruler, scissor, fan, laptop,
remote-control, bag, pants, shoes, hat
```

In the app, labels are translated to Portuguese for user feedback.

## Current features

The Android prototype includes:

* image capture using the camera;
* image selection using Android Photo Picker;
* local inference with TensorFlow Lite;
* support for multiple TFLite models;
* YOLO26n Float32 as the default model;
* YOLO26n Float16 as an experimental alternative;
* YOLOv8n Float32 as a comparative model;
* backend mode for comparison;
* letterbox preprocessing;
* semi-continuous CameraX detection;
* visual overlay with object, confidence, and inference time;
* Text-to-Speech in Portuguese;
* vibration feedback;
* cooldown to reduce repeated speech in continuous mode;
* basic TalkBack semantics.

## Main results summary

### Dataset images in emulator

| Metric                   | Result |
| ------------------------ | -----: |
| Evaluated images         |     20 |
| Correct                  |     17 |
| Partially correct        |      1 |
| Incorrect                |      1 |
| No detection             |      1 |
| Simple accuracy          |    85% |
| Correct + partial        |    90% |
| Approximate average time | 368 ms |

### Dataset images on physical device

| Metric                   |   Result |
| ------------------------ | -------: |
| Evaluated images         |       10 |
| Correct                  |        8 |
| Incorrect                |        1 |
| No detection             |        1 |
| Approximate average time | 167.3 ms |

### CameraX functional test

| Metric                          |   Result |
| ------------------------------- | -------: |
| Registered observations         |       10 |
| Semantically correct detections |       10 |
| Approximate average time        | 214.2 ms |
| Minimum observed time           |   111 ms |
| Maximum observed time           |   467 ms |
| Approximate average confidence  |    60.9% |

The CameraX evaluation was treated as a functional and qualitative test, not as a broad statistical evaluation.

### Final short model selection test

| Model           | Correct | Average confidence | Average time |
| --------------- | ------: | -----------------: | -----------: |
| YOLO26n Float32 |     3/3 |              92.7% |     138.7 ms |
| YOLO26n Float16 |     3/3 |              93.0% |     145.3 ms |
| YOLOv8n Float32 |     3/3 |              91.0% |     194.3 ms |

Based on these results and on the stability observed during development, YOLO26n Float32 was selected as the final default model.

## Project structure

```text
blind-assistance-app/
|-- app/
|   |-- src/
|       |-- main/
|           |-- assets/
|           |   |-- classroom_yolo26n_e50_best_float32.tflite
|           |   |-- classroom_yolo26n_e50_best_float16.tflite
|           |   |-- classroom_yolov8n_e50_best_float32.tflite
|           |   |-- labels.txt
|           |-- java/com/anonymous/blindassistanceapp/
|           |   |-- CameraPreview.kt
|           |   |-- MainActivity.kt
|           |   |-- YoloTfliteDetector.kt
|           |   |-- ui/theme/
|           |-- res/
|           |-- AndroidManifest.xml
|-- gradle/
|-- README.md
|-- build.gradle.kts
|-- settings.gradle.kts
```

## Main files

### `MainActivity.kt`

Main app screen, image capture, image selection, backend integration, on-device integration, Text-to-Speech, vibration, and app mode organization.

### `CameraPreview.kt`

Implements the semi-continuous CameraX detection mode. It opens the camera, captures frames, converts frames to Bitmap, runs the local detector, updates the overlay, and controls speech/vibration feedback.

### `YoloTfliteDetector.kt`

Loads the TFLite model, applies letterbox preprocessing, prepares the input buffer, runs local inference, and interprets the model output.

## How to run

1. Open the project in Android Studio.
2. Sync Gradle.
3. Connect an Android device or start an emulator.
4. Run the app.

The on-device and CameraX modes do not require the backend.

To test the backend mode, also run the FastAPI server from the related backend repository.

## Backend local address

When using the Android emulator, the app accesses the local backend through:

```text
http://10.0.2.2:8000
```

For a physical device, use the host machine local IP address on the same Wi-Fi network.

## Current status

* Android app functional;
* on-device inference working;
* CameraX mode working;
* Text-to-Speech working;
* vibration feedback working;
* backend mode preserved as baseline;
* final default model: YOLO26n Float32.

## Limitations

The app is still a research prototype and has limitations:

* it recognizes only the 20 dataset classes;
* it may confuse objects outside the trained classes;
* it has not been formally evaluated with blind or visually impaired users;
* real-world tests were performed with small samples;
* bounding boxes are not yet used for spatial guidance;
* advanced feedback for multiple objects is not implemented;
* Float16 still requires broader validation in the CameraX mode;
* battery consumption and thermal behavior were not formally evaluated.

## Future work

Possible future improvements include:

* evaluation with blind or visually impaired users;
* broader testing with real photos;
* further validation of YOLO26n Float16 in CameraX mode;
* battery and thermal evaluation;
* spatial guidance using bounding boxes;
* messages such as "move slightly left" or "move closer";
* improved feedback for multiple objects;
* training with more real-world images;
* comparison between larger backend models and smaller on-device models;
* separation between assistive mode and debugging mode.

## Related repository

The FastAPI backend was used as a baseline in the project.

Repository information is omitted for double-anonymous review.
