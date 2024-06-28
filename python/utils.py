import cv2
import calculations as c
import matplotlib.pyplot as plt
import matplotlib.animation as animation
from matplotlib import gridspec

def get_coords(face_landmarks, region):
    coords = []
    for index in region:
        x = face_landmarks.landmark[index].x
        y = face_landmarks.landmark[index].y
        coords.append( (x, y) )    
    return coords
    
def draw_fps(frame, fps):
    cv2.putText(frame, f'fps: {fps:.1f}', 
            org       = (10, 50), 
            fontFace  = cv2.FONT_HERSHEY_SIMPLEX, 
            fontScale = 1,
            color     = (0,0,255),
            thickness = 2,
            lineType  = cv2.LINE_8)

def draw_results(frame):
    height, width, _ = frame.shape

    cv2.putText(frame, f'l_EAR: {c.l_EAR:.3f}', 
            org       = (10, 50), 
            fontFace  = cv2.FONT_HERSHEY_SIMPLEX, 
            fontScale = 1,
            color     = (0,0,255),
            thickness = 2,
            lineType  = cv2.LINE_8)

    cv2.putText(frame, f'r_EAR: {c.r_EAR:.3f}', 
            org       = (width-230, 50), 
            fontFace  = cv2.FONT_HERSHEY_SIMPLEX, 
            fontScale = 1,
            color     = (0,0,255),
            thickness = 2,
            lineType  = cv2.LINE_8)

    cv2.putText(frame, f'DIR: {c.eye_DIR:.3f}', 
            org       = (10, 100), 
            fontFace  = cv2.FONT_HERSHEY_SIMPLEX, 
            fontScale = 1,
            color     = (0,0,255),
            thickness = 2,
            lineType  = cv2.LINE_8)


def runGraph():
    # Parameters
    x_len = 200       # Number of points to display

    # Create figure for plotting
    fig = plt.figure('Eye tracking')
    xs = list(range(0, 200))
    spec = gridspec.GridSpec(2, 2)

    ax1 = plt.subplot(spec[0, 0])
    ax1.title.set_text('Left Eye Blink')
    ax1.set_xticklabels([])
    ys1 = [0] * x_len
    ax1.set_ylim([0, 0.8])
    line1, = ax1.plot(xs, ys1)

    ax2 = plt.subplot(spec[0, 1])
    ax2.title.set_text('Right Eye Blink')
    ax2.set_xticklabels([])
    ys2 = [0] * x_len
    ax2.set_ylim([0, 0.8])
    line2, = ax2.plot(xs, ys2)
    
    ax3 = plt.subplot(spec[1, :])
    ax3.title.set_text('Eye Direction')
    ax3.set_xticklabels([])
    ys3 = [0] * x_len
    ax3.set_ylim([-0.6, 0.6])
    line3, = ax3.plot(xs, ys3)

    def animate(i, ys1, ys2, ys3):

        # Add values to list
        ys1.append(c.l_EAR)
        ys2.append(c.r_EAR)
        ys3.append(c.eye_DIR)

        # Limit list size to set number of items
        ys1 = ys1[-x_len:]
        ys2 = ys2[-x_len:]
        ys3 = ys3[-x_len:]

        # Update line with values from list
        line1.set_ydata(ys1)
        line2.set_ydata(ys2)
        line3.set_ydata(ys3)

        return line1, line2, line3


    # Set up plot to call animate() function periodically
    ani = animation.FuncAnimation(fig,
        animate,
        fargs=(ys1, ys2, ys3),
        interval=50,
        blit=True)
    plt.show()