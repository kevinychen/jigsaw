package com.kyc.jigsaw.classifier;

public interface Classifier {

    boolean isPiece(int rgb);

    static Classifier getDefault() {
        return new BlueClassifier();
    }
}
