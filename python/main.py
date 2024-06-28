import cv2
import mediapipe as mp
import threading

import time

from utils import *
from orders import send_orders
import calculations as c


LEFT_EYE_OUTLINE  = [263, 387, 385, 362, 380, 373]
RIGHT_EYE_OUTLINE = [33,  160, 158, 133, 153, 144]

LEFT_EYE_IRIS  = [362, 473, 263]
RIGHT_EYE_IRIS = [33,  468, 133]

if __debug__:
    x = threading.Thread(target=runGraph, daemon=True)
    x.start()

y = threading.Thread(target=send_orders, daemon=True)
y.start()


cap = cv2.VideoCapture(0)

start_time = time.time()
frame_counter = 0

with mp.solutions.face_mesh.FaceMesh(
    static_image_mode=False,    # Video input
    refine_landmarks=True,      # Add iris detection
    max_num_faces=1) as face_mesh:

    while True:

        ret, frame = cap.read() # Leer un frame de la webcam
        if ret == False:
            print("Ignoring empty camera frame.")
            continue

        frame_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        results = face_mesh.process(frame_rgb)
        frame = cv2.flip(frame, 1)

        if results.multi_face_landmarks:
            for face_landmarks in results.multi_face_landmarks:

                c.l_EAR = c.EAR(get_coords(face_landmarks, LEFT_EYE_OUTLINE))
                c.r_EAR = c.EAR(get_coords(face_landmarks, RIGHT_EYE_OUTLINE))
                c.eye_DIR = c.DIR(get_coords(face_landmarks, LEFT_EYE_IRIS), get_coords(face_landmarks, RIGHT_EYE_IRIS))

                # draw_results(frame)
        else:
            # If no faces detectected, fill with default values
            c.l_EAR = 1
            c.r_EAR = 1
            c.eye_DIR = 0
        
        frame_counter += 1
        fps = frame_counter / (time.time() - start_time)
        draw_fps(frame, fps)

        cv2.imshow('Eye tracking', frame)
        if cv2.waitKey(1) & 0xFF == 27:     # Press ESC
            break

cap.release()
cv2.destroyAllWindows()
