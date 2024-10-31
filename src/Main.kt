import processing.core.PApplet
import processing.core.PVector

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
        repeat(parameters.getFloat(Parameters.FloatValues.BallCount).toInt()) {
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

        val oldFloats = parameters.floatValues.map { Pair(it.key, it.value.get()) }.toMap()
        val oldBooleans = parameters.booleanValues.map { Pair(it.key, it.value.get()) }.toMap()
        parameters.update()

        val newCount = parameters.getFloat(Parameters.FloatValues.BallCount).toInt()
        val oldCount = oldFloats[Parameters.FloatValues.BallCount]!!.toInt()
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

        if (oldFloats[Parameters.FloatValues.BallRadius] != parameters.getFloat(Parameters.FloatValues.BallRadius) ||
            oldFloats[Parameters.FloatValues.LavaScale] != parameters.getFloat(Parameters.FloatValues.LavaScale) ||
            oldBooleans[Parameters.BooleanValues.ShowLava] != parameters.getBoolean(Parameters.BooleanValues.ShowLava)) {
            grid = Grid()
            balls.forEach { ball -> ball.assignCell() }
        }

        if (oldFloats[Parameters.FloatValues.ImgScale] != parameters.getFloat(Parameters.FloatValues.ImgScale)) {
            lava.createImg()
        }

        if (parameters.getBoolean(Parameters.BooleanValues.ShowLava)) {
            lava.update()
        }
    }

    private fun display() {
        background(100)
        fill(255)
        noStroke()
        rect(parameters.bounds.first.x, parameters.bounds.first.y,
            parameters.bounds.second.x, parameters.bounds.second.y)

        if (parameters.getBoolean(Parameters.BooleanValues.ShowLava)) {
            lava.display()
        }
        if (parameters.getBoolean(Parameters.BooleanValues.ShowBalls))
            balls.forEach { ball -> ball.display() }
        if (parameters.getBoolean(Parameters.BooleanValues.ShowGrid)) {
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