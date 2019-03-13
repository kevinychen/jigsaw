package com.kyc.jigsaw;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.opencv.core.Point;

import com.google.common.collect.ImmutableList;

public class PieceTester {

    public List<PieceSuggestion> findSuggestions(List<PositionedPiece> candidates, List<Optional<Piece>> surroundingPieces) {
        List<PieceSuggestion> suggestions = new ArrayList<>();
        List<Optional<Piece>> orientedSurroundingPieces = surroundingPieces.stream()
            .map(maybePiece -> maybePiece.map(piece -> orient(piece, true, 0)))
            .collect(Collectors.toList());
        for (PositionedPiece piece : candidates)
            for (int dir = 0; dir < 4; dir++) {
                double score = score(orient(piece.getPiece(), false, dir), orientedSurroundingPieces);
                suggestions.add(new PieceSuggestion(piece, score));
            }
        return suggestions.stream()
            .sorted(Comparator.comparingDouble(suggestion -> -suggestion.getScore()))
            .limit(10)
            .collect(Collectors.toList());
    }

    private static Piece orient(Piece piece, boolean flip, int dir) {
        List<Side> sides = new ArrayList<>(piece.getSides());
        if (flip) {
            sides = ImmutableList.of(
                reverse(flip(sides.get(2))),
                reverse(flip(sides.get(1))),
                reverse(flip(sides.get(0))),
                reverse(flip(sides.get(3))));
        }
        return new Piece(ImmutableList.<Side> builder()
            .addAll(sides.subList(dir, 4))
            .addAll(sides.subList(0, dir))
            .build());
    }

    private static Side flip(Side side) {
        return new Side(side.getPoints().stream()
            .map(p -> new Point(-p.x, p.y))
            .collect(Collectors.toList()));
    }

    private double score(Piece piece, List<Optional<Piece>> surroundingPieces) {
        double minScore = Double.MAX_VALUE;
        for (int i = 0; i < 4; i++)
            if (surroundingPieces.get(i).isPresent()) {
                List<Point> points = normalize(piece.getSides().get(i).getPoints());
                List<Point> neighboringPoints = normalize(reverse(surroundingPieces.get(i).get().getSides().get((i + 2) % 4)).getPoints());
                double score = 1.0 / difference(points, neighboringPoints);
                if (score < minScore)
                    minScore = score;
            }
        return minScore;
    }

    private static Side reverse(Side side) {
        List<Point> reversed = new ArrayList<>(side.getPoints());
        Collections.reverse(reversed);
        return new Side(reversed);
    }

    private static List<Point> normalize(List<Point> points) {
        Point first = points.get(0);
        Point last = points.get(points.size() - 1);
        double baseX = last.x - first.x, baseY = last.y - first.y;
        double scale = Math.hypot(baseX, baseY);
        double angle = Math.atan2(baseY, baseX);
        return points.stream().map(p -> {
            double x = p.x - first.x, y = p.y - first.y;
            return new Point((Math.cos(-angle) * x - Math.sin(-angle) * y) / scale, (Math.sin(-angle) * x + Math.cos(-angle) * y) / scale);
        }).collect(Collectors.toList());
    }

    private static double difference(List<Point> side1, List<Point> side2) {
        double difference1 = 0;
        for (Point p : side1)
            difference1 += closestDist2(side2, p);
        double difference2 = 0;
        for (Point p : side2)
            difference2 += closestDist2(side1, p);
        return difference1 / side1.size() + difference2 / side2.size();
    }

    private static double closestDist2(List<Point> points, Point target) {
        double closestDist2 = Double.MAX_VALUE;
        for (Point p : points) {
            double dx = p.x - target.x, dy = p.y - target.y;
            double dist2 = dx * dx + dy * dy;
            if (dist2 < closestDist2)
                closestDist2 = dist2;
        }
        return closestDist2;
    }
}
