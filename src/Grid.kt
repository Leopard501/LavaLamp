import processing.core.PVector
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

class Grid {

    class Cell {
        val balls = ArrayList<Ball>()
    }

    private var cellSize: PVector
    private var cells: Array<Array<Cell>>

    init {
        val boundsSize = parameters.bounds.second - parameters.bounds.first
        println("${1 / parameters[Parameters.FloatValues.ImgScale]}, ${1 - parameters[Parameters.FloatValues.LavaScale]} ${(1 / parameters[Parameters.FloatValues.ImgScale]) /
                (1 - parameters[Parameters.FloatValues.LavaScale])}")
        val shortSide = if (parameters[Parameters.BooleanValues.ShowLava]) {
            max(
                parameters[Parameters.FloatValues.BallRadius] * 2,
                ((1 / parameters[Parameters.FloatValues.ImgScale]) /
                    (1 - parameters[Parameters.FloatValues.LavaScale]))
                        / max(boundsSize.x, boundsSize.y))
        } else {
            parameters[Parameters.FloatValues.BallRadius] * 2
        }
        val ratio = max(boundsSize.x, boundsSize.y) / min(boundsSize.x, boundsSize.y)
        cellSize = if (boundsSize.x > boundsSize.y) {
            PVector(shortSide * ratio, shortSide)
        } else {
            PVector(shortSide, shortSide * ratio)
        }

        cells = Array((boundsSize.x / cellSize.x).toInt()) {
            Array((boundsSize.y / cellSize.y).toInt()) {
                Cell()
            }
        }
    }

    fun getNeighborBalls(position: PVector): Array<Ball> {
        val a = ArrayList<Ball>()
        val t = getCellIdx(position)

        // attempts to add all balls from cell and neighbors
        for (x in -1 .. 1) {
            for (y in -1 .. 1) {
                try { a.addAll(cells[t.first + x][t.second + y].balls) } catch (_: IndexOutOfBoundsException) { }
            }
        }

        return a.toTypedArray()
    }

    fun getCell(position: PVector): Cell {
        var p = Pair(
            ((position.x - parameters.bounds.first.x) / cellSize.x).toInt(),
            ((position.y - parameters.bounds.first.y) / cellSize.y).toInt())
        if (p.first >= grid.cells.size) {
            p = Pair((grid.cells.size - 1), p.second)
        }; if (p.second >= grid.cells.size) {
            p = Pair(p.first, grid.cells.size - 1)
        }
        return cells[p.first][p.second]
    }

    private fun getCellIdx(position: PVector): Pair<Int, Int> {
        var p = Pair(
            ((position.x - parameters.bounds.first.x) / cellSize.x).toInt(),
            ((position.y - parameters.bounds.first.y) / cellSize.y).toInt())
        if (p.first >= grid.cells.size) {
            p = Pair((grid.cells.size - 1), p.second)
        }; if (p.second >= grid.cells.size) {
            p = Pair(p.first, grid.cells.size - 1)
        }
        return p
    }

    fun colorize(position: PVector): Color {
        val idx = grid.getCellIdx(position)
        return Color(
            (idx.first / grid.getArraySize().toFloat()).coerceIn(0f, 1f),
            0f,
            (idx.second / grid.getArraySize().toFloat()).coerceIn(0f, 1f))
    }

    private fun getArraySize(): Int {
        return cells.size
    }

    fun display() {
        app.stroke(0)
        app.strokeWeight(1f)
        for (i in cells.indices) {
            app.line(i * cellSize.x, parameters.bounds.first.y,
                i * cellSize.x, parameters.bounds.second.y)
            app.line(parameters.bounds.first.x, i * cellSize.y,
                parameters.bounds.second.x, i * cellSize.y)
        }
    }
}