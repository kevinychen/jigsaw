package com.kyc.jigsaw;

import java.util.List;

import org.opencv.core.Point;

import lombok.Data;

@Data
public class Side {
    private final List<Point> points;
}
