package com.kyc.jigsaw;

import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class JigsawResource implements JigsawService {

    @Override
    public String ping() {
        return "pong";
    }

    @Override
    public void solve(InputStream upImage, InputStream leftImage, InputStream rightImage, InputStream downImage)
            throws IOException {
        System.out.println(ImageIO.read(upImage));
        System.out.println(ImageIO.read(leftImage));
        System.out.println(ImageIO.read(rightImage));
        System.out.println(ImageIO.read(downImage));
    }
}
