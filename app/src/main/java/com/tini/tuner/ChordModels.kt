package com.tini.tuner

/**
 * Chord finger positions: 6 strings low-to-high (E A D G B e), -1 = muted, 0 = open.
 */
data class ChordDiagram(
    val name: String,
    val displayName: String,
    val frets: List<Int>, // size 6
    val fingers: List<Int> = emptyList(), // optional finger numbers at fret positions
    val baseFret: Int = 1 // for barre chords shown relative to this fret
)

object ChordLibrary {
    val chords: List<ChordDiagram> = listOf(
        ChordDiagram("C", "C", listOf(-1, 3, 2, 0, 1, 0)),
        ChordDiagram("D", "D", listOf(-1, -1, 0, 2, 3, 2)),
        ChordDiagram("E", "E", listOf(0, 2, 2, 1, 0, 0)),
        ChordDiagram("G", "G", listOf(3, 2, 0, 0, 0, 3)),
        ChordDiagram("A", "A", listOf(-1, 0, 2, 2, 2, 0)),
        ChordDiagram("Am", "Am", listOf(-1, 0, 2, 2, 1, 0)),
        ChordDiagram("Em", "Em", listOf(0, 2, 2, 0, 0, 0)),
        ChordDiagram("Dm", "Dm", listOf(-1, -1, 0, 2, 3, 1)),
        ChordDiagram("F", "F", listOf(1, 3, 3, 2, 1, 1), baseFret = 1),
        ChordDiagram("Bm", "Bm", listOf(-1, 2, 4, 4, 3, 2), baseFret = 2),
        ChordDiagram("B7", "B7", listOf(-1, 2, 1, 2, 0, 2), baseFret = 2),
        ChordDiagram("C7", "C7", listOf(-1, 3, 2, 3, 1, 0)),
        ChordDiagram("D7", "D7", listOf(-1, -1, 0, 2, 1, 2)),
        ChordDiagram("E7", "E7", listOf(0, 2, 0, 1, 0, 0)),
        ChordDiagram("G7", "G7", listOf(3, 2, 0, 0, 0, 1)),
        ChordDiagram("A7", "A7", listOf(-1, 0, 2, 0, 2, 0)),
        ChordDiagram("Am7", "Am7", listOf(-1, 0, 2, 0, 1, 0)),
        ChordDiagram("Em7", "Em7", listOf(0, 2, 0, 0, 0, 0)),
        ChordDiagram("Dm7", "Dm7", listOf(-1, -1, 0, 2, 1, 1)),
        ChordDiagram("Cmaj7", "Cmaj7", listOf(-1, 3, 2, 0, 0, 0)),
        ChordDiagram("Dmaj7", "Dmaj7", listOf(-1, -1, 0, 2, 2, 2)),
        ChordDiagram("Amaj7", "Amaj7", listOf(-1, 0, 2, 1, 2, 0)),
        ChordDiagram("Gsus4", "Gsus4", listOf(3, 2, 0, 0, 1, 3)),
        ChordDiagram("Asus2", "Asus2", listOf(-1, 0, 2, 2, 0, 0)),
        ChordDiagram("Dsus4", "Dsus4", listOf(-1, -1, 0, 2, 3, 3)),
        ChordDiagram("Esus4", "Esus4", listOf(0, 2, 2, 2, 0, 0)),
        ChordDiagram("Fmaj7", "Fmaj7", listOf(1, 3, 3, 2, 1, 0), baseFret = 1),
        ChordDiagram("Cm", "Cm", listOf(-1, 3, 5, 5, 4, 3), baseFret = 3),
        ChordDiagram("Fm", "Fm", listOf(1, 3, 3, 1, 1, 1), baseFret = 1),
        ChordDiagram("Gmaj7", "Gmaj7", listOf(3, 2, 0, 0, 0, 2))
    )
}
