import processing.core.PApplet
import processing.core.PConstants
import processing.core.PVector
import java.awt.Color
import java.util.*

class Parameters: Cloneable {

    public override fun clone(): Parameters {
        return super.clone() as Parameters
    }

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
        private val v: Parameter<Float>,
        private val name: String,
        private val min: Float,
        private val max: Float) {

        private var held = false

        fun display() {
            val areaWidth = app.width - parameters.bounds.second.x
            val start = parameters.bounds.second.x + 0.1f * areaWidth
            val end = app.width - 0.1f * areaWidth

            app.stroke(0f)
            app.strokeWeight(2f)
            app.line(start, height, end, height)

            app.fill(parameters.ballColor.rgb)
            app.noStroke()
            app.circle(start + areaWidth * 0.8f * v.get() / (max - min), height, 10f)

            app.textAlign(PConstants.CENTER)
            app.fill(0f)
            app.text(name, start + areaWidth * 0.4f, height - 10f)
            app.text(v.get(), start + areaWidth * 0.8f * v.get() / (max - min), height + 15f)
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
            v.set(
                PApplet.map(app.mouseX.toFloat(),
                    parameters.bounds.second.x + 0.1f * areaWidth,
                    app.width - 0.1f * areaWidth,
                    min, max).coerceIn(min, max)
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

    enum class FloatValues(val id: String, val initial: Float, val min: Float, val max: Float) {
        BallRed("Red", 1f, 0f, 1f),
        BallGreen("Green", 0f, 0f, 1f),
        BallBlue("Blue", 0f, 0f, 1f),
        BallAlpha("Alpha", 100f, 0f, 255f),
        BallCount("Ball Count", 100f, 1f, 2500f),
        BallRadius("Ball Radius", 10f, 1f, 100f),
        BallStartingVel("Starting Velocity", 3f, 0f, 50f),
        BallSpring("Springiness", 1f, 0f, 5f),
        Gravity("Gravity", 0.1f, 0f, 1f),
        Dampening("Dampening", 0.9f, 0f, 1.1f),
        ImgScale("Image Scale", 0.2f, 0f, 1f),
        LavaScale("Lava Scale", 0.5f, 0f, 0.99f),
    }

    enum class BooleanValues(val id: String, val initial: Boolean) {
        DampeningOnCollisions("Dampening on Collisions", true),
        ShowBalls("Show Balls", true),
        ShowLava("Show Lava", false),
        ClampLava("Clamp Lava", false),
        ShowGrid("Show Grid", true),
    }

    var floatValues = EnumMap(FloatValues.entries.associateWith { Parameter(it.initial) })
    var booleanValues = EnumMap(BooleanValues.entries.associateWith { Parameter(it.initial) })

    var ballColor = getColor()
    var bounds = Pair(PVector(0f, 0f), PVector(900f, 900f))

    private val sliders = floatValues.map {
        e -> Slider((e.key.ordinal + 1) * 40f, e.value, e.key.id, e.key.min, e.key.max)
    }

    private val checkboxes = booleanValues.map {
        e -> Checkbox(sliders.last().height + (e.key.ordinal + 2) * 20f, e.value, e.key.id)
    }

    var sliderHeld = false;

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
            floatValues.getValue(FloatValues.BallRed).get(),
            floatValues.getValue(FloatValues.BallGreen).get(),
            floatValues.getValue(FloatValues.BallBlue).get(),
        )
    }

    operator fun get(key: FloatValues): Float =
        floatValues.getValue(key).get()

    operator fun get(key: BooleanValues): Boolean =
        booleanValues.getValue(key).get()
}