package com.kyc.jigsaw;

import java.util.List;

import lombok.Data;

@Data
public class Piece {

    /**
     * The WEST, NORTH, EAST, and SOUTH sides, in that order.
     */
    private final List<Side> sides;
}
