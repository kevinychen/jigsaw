package com.kyc.jigsaw.server;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kyc.jigsaw.BulkPieceParser;
import com.kyc.jigsaw.Piece;
import com.kyc.jigsaw.PieceParser;
import com.kyc.jigsaw.PieceSuggestion;
import com.kyc.jigsaw.PieceTester;
import com.kyc.jigsaw.PositionedPiece;
import com.kyc.jigsaw.api.JigsawService;

import lombok.Data;

@Data
public class JigsawResource implements JigsawService {

    private static final Logger log = LoggerFactory.getLogger(JigsawResource.class);

    private final PieceParser parser;
    private final BulkPieceParser bulkParser;
    private final PieceTester tester;

    @Override
    public String ping() {
        return "pong";
    }

    @Override
    public void solve(InputStream upImage, InputStream leftImage, InputStream rightImage, InputStream downImage)
            throws IOException {
        log.info("Fetching images from client...");
        solve(Stream.of(leftImage, upImage, rightImage, downImage)
            .map(maybeStream -> {
                return Optional.ofNullable(maybeStream).map(stream -> {
                    try {
                        return ImageIO.read(stream);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            })
            .collect(Collectors.toList()));
    }

    private void solve(List<Optional<BufferedImage>> images) throws IOException {
        log.info("Finding suggestions...");
        List<PositionedPiece> candidates = bulkParser.load();
        List<Optional<Piece>> surroundingPieces = images.stream()
            .map(image -> image.map(parser::parse))
            .collect(Collectors.toList());

        List<PieceSuggestion> suggestions = tester.findSuggestions(candidates, surroundingPieces);
        if (suggestions.isEmpty()) {
            log.warn("No suggestions found!");
            return;
        }

        PieceSuggestion bestSuggestion = suggestions.get(0);
        log.info("Found suggestion with score {}", bestSuggestion.getScore());

        PositionedPiece bestPiece = bestSuggestion.getPiece();
        BufferedImage solutionImage = bulkParser.loadImage(bestPiece.getName());
        Graphics g = solutionImage.getGraphics();
        g.setColor(Color.green);
        for (int r = 40; r <= 60; r++)
            g.drawOval(bestPiece.getX() - r, bestPiece.getY() - r, 2 * r, 2 * r);
        File tempFile = File.createTempFile("temp-solution-", ".png");
        ImageIO.write(solutionImage, "png", tempFile);
        Desktop.getDesktop().open(tempFile);
    }
}
