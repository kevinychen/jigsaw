package com.kyc.jigsaw;

import lombok.Data;

@Data
public class PieceSuggestion {
    private final PositionedPiece piece;
    /**
     * How good the match is. Over 2000 is a good match, over 3000 is a great one.
     */
    private final double score;
}
