package cat.mrtxema.covid.chart;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public interface Chart {

    JFrame display();

    void saveTo(File file) throws IOException;

    BufferedImage getImage();
}
