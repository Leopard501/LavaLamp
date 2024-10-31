import processing.core.PApplet
import processing.core.PVector
import java.util.*
import kotlin.collections.ArrayList

var app = Main()

var mousePressedPulse = false
var mouseReleasedPulse = false

lateinit var parameters: Parameters
val balls: ArrayList<Ball> = ArrayList()
lateinit var lava: Lava
lateinit var grid: Grid

fun main() {
    PApplet.main("Main")
}

class Main: PApplet() {

    override fun settings() {
        size(1100, 900)
        noSmooth()

        app = this
    }

    override fun setup() {
        surface.setTitle("Lava Lamp")
        rectMode(CORNERS)

        parameters = Parameters()
        repeat(parameters[Parameters.FloatValues.BallCount].toInt()) {
            balls.add(Ball())
        }
        grid = Grid()
        balls.forEach { ball -> ball.assignCell() }
        lava = Lava()
    }

    override fun draw() {
        update()
        display()

        mousePressedPulse = false
        mouseReleasedPulse = false
    }

    private fun update() {
        balls.forEach { ball -> ball.update() }

        val oldFloats = EnumMap(parameters.floatValues.map { Pair(it.key, it.value.get()) }.toMap())
        val oldBooleans = EnumMap(parameters.booleanValues.map { Pair(it.key, it.value.get()) }.toMap())
        parameters.update()

        val newCount = parameters[Parameters.FloatValues.BallCount].toInt()
        val oldCount = oldFloats.getValue(Parameters.FloatValues.BallCount).toInt()
        if (oldCount > newCount) {
            balls.shuffle()
            balls.subList(newCount-1, oldCount-1).clear()
            grid = Grid()
            balls.forEach { ball -> ball.assignCell() }
        } else if (newCount > oldCount) {
            repeat(newCount - oldCount) {
                val b = Ball()
                balls.add(b)
                b.assignCell()
            }
        }

        if (oldFloats.getValue(Parameters.FloatValues.BallRadius) != parameters[Parameters.FloatValues.BallRadius] ||
            oldFloats.getValue(Parameters.FloatValues.LavaScale) != parameters[Parameters.FloatValues.LavaScale] ||
            oldBooleans.getValue(Parameters.BooleanValues.ShowLava) != parameters[Parameters.BooleanValues.ShowLava]) {
            grid = Grid()
            balls.forEach { ball -> ball.assignCell() }
        }

        if (oldFloats[Parameters.FloatValues.ImgScale] != parameters[Parameters.FloatValues.ImgScale]) {
            lava.createImg()
        }

        if (parameters[Parameters.BooleanValues.ShowLava]) {
            lava.update()
        }
    }

    private fun display() {
        background(100)
        fill(255)
        noStroke()
        rect(parameters.bounds.first.x, parameters.bounds.first.y,
            parameters.bounds.second.x, parameters.bounds.second.y)

        if (parameters[Parameters.BooleanValues.ShowLava]) {
            lava.display()
        }
        if (parameters[Parameters.BooleanValues.ShowBalls])
            balls.forEach { ball -> ball.display() }
        if (parameters[Parameters.BooleanValues.ShowGrid]) {
            grid.display()
        }

        parameters.display()
    }

    override fun mousePressed() {
        mousePressedPulse = true
    }

    override fun mouseReleased() {
        mouseReleasedPulse = true
    }
}

// https://github.com/nickoala/kproc/blob/master/balls/src/procexxing.kt
operator fun PVector.plus(v: PVector): PVector {
    return PVector.add(this, v)
}

operator fun PVector.minus(v: PVector): PVector {
    return PVector.sub(this, v)
}

operator fun PVector.times(n: Float): PVector {
    return PVector.mult(this, n)
}