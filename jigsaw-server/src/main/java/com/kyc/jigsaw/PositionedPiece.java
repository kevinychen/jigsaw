package com.kyc.jigsaw;

import lombok.Data;

@Data
public final class PositionedPiece {

    private final String name;
    private final int x;
    private final int y;
    private final Piece piece;
}
