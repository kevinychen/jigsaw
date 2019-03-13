package com.kyc.jigsaw.classifier;

import java.awt.Color;

public class BlueClassifier implements Classifier {

    @Override
    public boolean isPiece(int rgb) {
        Color color = new Color(rgb);
        return color.getBlue() > color.getRed() - 64;
    }
}
