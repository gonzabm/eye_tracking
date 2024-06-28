import numpy as np

l_EAR = 0
r_EAR = 0
eye_DIR = 0

def EAR(coords):
    d1 = np.linalg.norm(np.array(coords[1]) - np.array(coords[5]))
    d2 = np.linalg.norm(np.array(coords[2]) - np.array(coords[4]))
    d3 = np.linalg.norm(np.array(coords[0]) - np.array(coords[3]))
    return (d1 + d2) / (2 * d3)

def DIR(coords1, coords2):
    d1 = np.linalg.norm(np.array(coords1[0]) - np.array(coords1[1]))
    d2 = np.linalg.norm(np.array(coords1[0]) - np.array(coords1[2]))
    l_eye_DIR = 2* d1 / d2 - 1

    d1 = np.linalg.norm(np.array(coords2[0]) - np.array(coords2[1]))
    d2 = np.linalg.norm(np.array(coords2[0]) - np.array(coords2[2]))
    r_eye_DIR = 2 * d1 / d2 - 1

    if l_eye_DIR > 0 and r_eye_DIR > 0:
        return max(l_eye_DIR, r_eye_DIR)
    elif l_eye_DIR < 0 and r_eye_DIR < 0:
        return min(l_eye_DIR, r_eye_DIR)
    else:
        return (l_eye_DIR + r_eye_DIR) / 2