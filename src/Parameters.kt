import processing.core.PApplet
import processing.core.PConstants
import processing.core.PVector
import java.awt.Color
import java.util.*
import kotlin.math.pow

enum class FloatValue(val id: String, val initial: Float, val min: Float, val max: Float, val scale: Int) {
    BallRed("Red", 1f, 0f, 1f, 1),
    BallGreen("Green", 0f, 0f, 1f, 1),
    BallBlue("Blue", 0f, 0f, 1f, 1),
    BallAlpha("Alpha", 100f, 0f, 255f, 1),
    BallCount("Ball Count", 100f, 1f, 2500f, 3),
    BallRadius("Ball Radius", 10f, 1f, 100f, 1),
    BallStartingVel("Starting Velocity", 3f, 0f, 50f, 1),
    BallSpring("Springiness", 1f, 0f, 5f, 1),
    Gravity("Gravity", 0.1f, 0f, 1f, 2),
    Dampening("Dampening", 0.1f, -0.1f, 1f, 1),
    ImgScale("Image Scale", 0.2f, 0f, 1f, 1),
    LavaScale("Lava Scale", 50f, 1f, 500f, 1),
}

enum class BooleanValues(val id: String, val initial: Boolean) {
    DampeningOnCollisions("Dampening on Collisions", true),
    ShowBalls("Show Balls", true),
    ShowLava("Show Lava", false),
    ClampLava("Clamp Lava", false),
    ShowGrid("Show Grid", false),
}

class Parameters {

    data class Parameter<T>(private var v: T) {
        fun get(): T {
            return v
        }
        fun set(new: T) {
            v = new
        }
    }

    class Slider(
        val height: Float,
        private val parameter: Parameter<Float>,
        private val value: FloatValue) {

        private var held = false

        fun display() {
            val areaWidth = app.width - parameters.bounds.second.x
            val start = parameters.bounds.second.x + 0.1f * areaWidth
            val end = app.width - 0.1f * areaWidth
            val s = ((parameter.get() - value.min) / (value.max - value.min)).pow(1 / value.scale.toFloat())

            app.stroke(0f)
            app.strokeWeight(2f)
            app.line(start, height, end, height)

            app.fill(parameters.ballColor.rgb)
            app.noStroke()
            app.circle(start + areaWidth * 0.8f * s,
                height, 10f)

            app.textAlign(PConstants.CENTER)
            app.fill(0f)
            app.text(value.id, start + areaWidth * 0.4f, height - 10f)
            app.text(parameter.get(),
                start + areaWidth * 0.8f * s,
                height + 15f)
        }

        fun update() {
            if (!held && app.mousePressed &&
                app.mouseX > parameters.bounds.second.x &&
                app.mouseY > height - 10f &&
                app.mouseY < height + 10f &&
                !parameters.sliderHeld) {
                held = true
                parameters.sliderHeld = true
            }; if (held && !app.mousePressed) {
                held = false
                parameters.sliderHeld = false
            }

            if (!held) return

            val areaWidth = app.width - parameters.bounds.second.x
            val s = PApplet.map(app.mouseX.toFloat(),
                parameters.bounds.second.x + 0.1f * areaWidth,
                app.width - 0.1f * areaWidth,
                0f, 1f
            ).coerceIn(0f, 1f)
            parameter.set(
                s.pow(value.scale) * (value.max - value.min) + value.min
            )
        }
    }

    class Checkbox(
        private val height: Float,
        private val v: Parameter<Boolean>,
        private val name: String
    ) {

        fun display() {
            val areaWidth = app.width - parameters.bounds.second.x
            val start = parameters.bounds.second.x + 0.1f * areaWidth

            app.stroke(0f)
            app.strokeWeight(2f)
            if (v.get()) app.fill(parameters.ballColor.rgb)
            else app.noFill()
            app.rect(start, height - 10f, start + 10f, height)

            app.textAlign(PConstants.LEFT)
            app.fill(0f)
            app.text(name, start + 10f + 10f, height)
        }

        fun update() {
            if (!(mousePressedPulse &&
                app.mouseX > parameters.bounds.second.x &&
                app.mouseY > height - 10f &&
                app.mouseY < height + 10f))
                return

            v.set(!v.get())
        }
    }

    var floatValue = EnumMap(FloatValue.entries.associateWith { Parameter(it.initial) })
    var booleanValues = EnumMap(BooleanValues.entries.associateWith { Parameter(it.initial) })

    var ballColor = getColor()
    var bounds = Pair(PVector(0f, 0f), PVector(900f, 900f))

    private val sliders = floatValue.map {
        e -> Slider((e.key.ordinal + 1) * 40f, e.value, e.key)
    }

    private val checkboxes = booleanValues.map {
        e -> Checkbox(sliders.last().height + (e.key.ordinal + 2) * 20f, e.value, e.key.id)
    }

    var sliderHeld = false

    fun display() {
        sliders.forEach { slider -> slider.display() }
        checkboxes.forEach { checkbox -> checkbox.display() }
    }

    fun update() {
        sliders.forEach { slider -> slider.update() }
        checkboxes.forEach { checkbox -> checkbox.update() }

        ballColor = getColor()
    }

    private fun getColor(): Color {
        return Color(
            floatValue.getValue(FloatValue.BallRed).get(),
            floatValue.getValue(FloatValue.BallGreen).get(),
            floatValue.getValue(FloatValue.BallBlue).get(),
        )
    }

    operator fun get(key: FloatValue): Float =
        floatValue.getValue(key).get()

    operator fun get(key: BooleanValues): Boolean =
        booleanValues.getValue(key).get()
}