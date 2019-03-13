package com.kyc.jigsaw;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.kyc.jigsaw.classifier.Classifier;

import lombok.Data;

@Data
public class BulkPieceParser {

    private static final Logger log = LoggerFactory.getLogger(BulkPieceParser.class);
    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    private final Classifier classifier;

    public List<PositionedPiece> load() {
        List<PositionedPiece> pieces = new ArrayList<>();
        try {
            for (File file : new File("data/pieces").listFiles())
                pieces.addAll(mapper.readValue(file, Pieces.class).getPieces());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return pieces;
    }

    public BufferedImage loadImage(String name) {
        try {
            return ImageIO.read(new File("data/images", name));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<PositionedPiece> bulkParse(String name, BufferedImage image) throws IOException {
        boolean[][] used = new boolean[image.getHeight()][image.getWidth()];
        PieceParser parser = new PieceParser(classifier);
        List<PositionedPiece> pieces = new ArrayList<>();
        for (int x = 0; x < image.getWidth(); x++)
            for (int y = 0; y < image.getHeight(); y++)
                if (classifier.isPiece(image.getRGB(x, y)) && !used[y][x]) {
                    List<Point> ff = new ArrayList<>();
                    ff.add(new Point(x, y));
                    int minX = Integer.MAX_VALUE;
                    int minY = Integer.MAX_VALUE;
                    int maxX = Integer.MIN_VALUE;
                    int maxY = Integer.MIN_VALUE;
                    int size = 0;
                    while (!ff.isEmpty()) {
                        Point p = ff.remove(ff.size() - 1);
                        if (p.x >= 0 && p.x < image.getWidth() && p.y >= 0 && p.y < image.getHeight() && classifier.isPiece(image.getRGB(p.x, p.y))
                                && !used[p.y][p.x]) {
                            used[p.y][p.x] = true;
                            size++;
                            for (int dx = -2; dx <= 2; dx++)
                                for (int dy = -2; dy <= 2; dy++)
                                    ff.add(new Point(p.x + dx, p.y + dy));
                            if (p.x < minX)
                                minX = p.x;
                            if (p.y < minY)
                                minY = p.y;
                            if (p.x > maxX)
                                maxX = p.x;
                            if (p.y > maxY)
                                maxY = p.y;
                        }
                    }
                    if (size > 1000 && minX >= 5 && minY >= 5 && maxX < image.getWidth() - 5 && maxY < image.getHeight() - 5) {
                        try {
                            pieces.add(new PositionedPiece(
                                name,
                                (minX + maxX) / 2,
                                (minY + maxY) / 2,
                                parser.parse(image.getSubimage(minX - 5, minY - 5, maxX - minX + 10, maxY - minY + 10))));
                        } catch (RuntimeException e) {
                            log.warn("Failed to parse piece", e);
                        }
                    }
                }
        return pieces;
    }

    @Data
    private static final class Pieces {

        final List<PositionedPiece> pieces;
    }

    public static void main(String[] args) throws IOException {
        BulkPieceParser parser = new BulkPieceParser(Classifier.getDefault());
        File piecesDir = new File("data/pieces");
        piecesDir.mkdirs();
        for (File file : new File("data/images").listFiles()) {
            String name = file.getName();
            log.info("Parsing {}", name);
            List<PositionedPiece> pieces = parser.bulkParse(name, ImageIO.read(file));
            mapper.writeValue(new File(piecesDir, name.substring(0, name.lastIndexOf('.')) + ".yml"), new Pieces(pieces));
        }
    }
}
