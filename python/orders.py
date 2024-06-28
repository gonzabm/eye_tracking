import requests
import calculations as c
import time

ip = '192.168.203.17'
close_eye_threshold  = 0.2
look_left_threshold  = 0.4
look_right_threshold = -0.4

rps = 1
    
def send_to_ESP32(device, state):
    requests.get('http://' + ip + ':80/update', { 'device': device, 'state': state})

def send_orders():
    time.sleep(3)
    send_to_ESP32("brakes", 1)  # Desactivar los frenos para poder mover la silla

    timer1 = None
    timer2 = None
    brakes_activated = True
    while True:
        time.sleep(1 / rps)
        # Cerrar los dos ojos -> Activar o desactivar freno
        if c.l_EAR < close_eye_threshold and c.r_EAR < close_eye_threshold:
            timer2 = None
            if timer1 is None:
                timer1 = time.time()
                continue
            if time.time() - timer1 > 2:
                send_to_ESP32("brakes", 0 if brakes_activated else 1)
                send_to_ESP32("lMotor", 0)
                send_to_ESP32("rMotor", 0)
                brakes_activated = not brakes_activated
                timer1 = None
                continue
            else:
                continue
        else:
            timer1 = None
        
        if c.l_EAR < close_eye_threshold: # Ojo izquierdo cerrado -> Avanzar silla
            timer1 = None
            if timer2 is None:
                timer2 = time.time()
                continue
            if time.time() - timer2 > 0.3:
                send_to_ESP32("lMotor", 1)
                send_to_ESP32("rMotor", 1)
                timer2 = None
                continue
            else:
                continue
        else:
            timer2 = None

        if c.eye_DIR > look_left_threshold:    # Mirar a la izquierda -> Girar a la izquierda
            send_to_ESP32("lMotor", 1)
            send_to_ESP32("rMotor", 0)
        elif c.eye_DIR < look_right_threshold: # Mirar a la derecha -> Girar a la derecha
            send_to_ESP32("lMotor", 0)
            send_to_ESP32("rMotor", 1)
        else:                                  # No hay acciones -> Detener la silla
            send_to_ESP32("lMotor", 0)
            send_to_ESP32("rMotor", 0)
