import processing.core.PApplet
import processing.core.PVector
import java.awt.Color
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

var app = Main()

var mousePressedPulse = false
var mouseReleasedPulse = false

val balls: ArrayList<Ball> = ArrayList()

lateinit var parameters: Parameters
lateinit var lava: Lava
lateinit var grid: Grid

var saving = false

fun main() {
    PApplet.main("Main")
}

class Main: PApplet() {

    override fun settings() {
        size(1100, 900)
//        noSmooth()

        app = this
    }

    override fun setup() {
        surface.setTitle("Lava Lamp")
        rectMode(CORNERS)

        parameters = Parameters()
        repeat(parameters[FloatValue.BallCount].toInt()) {
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

        val newCount = parameters[FloatValue.BallCount].toInt()
        val oldCount = oldFloats.getValue(FloatValue.BallCount).toInt()
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

        if (oldFloats.getValue(FloatValue.BallRadius) != parameters[FloatValue.BallRadius] ||
            oldFloats.getValue(FloatValue.LavaScale) != parameters[FloatValue.LavaScale] ||
            oldBooleans.getValue(BooleanValues.ShowLava) != parameters[BooleanValues.ShowLava]) {
            grid = Grid()
            balls.forEach { ball -> ball.assignCell() }
        }

        if (parameters[BooleanValues.ShowLava]) {
            lava = Lava()
        }
    }

    private fun display() {
        if (parameters[BooleanValues.ShowBackground]) background(100)
        fill(255)
        noStroke()
        if (parameters[BooleanValues.ShowBackground]) {
            rect(parameters.bounds.first.x, parameters.bounds.first.y,
                parameters.bounds.second.x, parameters.bounds.second.y)
        }

        if (parameters[BooleanValues.ShowLava])
            lava.display()
        if (parameters[BooleanValues.ShowBalls])
            balls.forEach { ball -> ball.display() }
        if (parameters[BooleanValues.ShowGrid])
            grid.display()

        parameters.display()

        if (saving) {
            val fileName = "${File("").absolutePath}/images/img_${hour()}_${minute()}_${second()}.png"
            save(fileName)
            saving = false
        }
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

operator fun Color.plus(c: Color): Color {
    return Color(
        (this.red / 255f + c.red / 255f).coerceIn(0f, 1f),
        (this.green / 255f + c.green / 255f).coerceIn(0f, 1f),
        (this.blue / 255f + c.blue / 255f).coerceIn(0f, 1f)
    )
}

operator fun Color.times(n: Float): Color {
    return Color(
        ((this.red / 255f) * n).coerceIn(0f, 1f),
        ((this.green / 255f) * n).coerceIn(0f, 1f),
        ((this.blue / 255f) * n).coerceIn(0f, 1f),
    )
}