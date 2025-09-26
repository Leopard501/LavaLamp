import processing.core.PApplet
import processing.core.PConstants
import processing.core.PVector
import java.awt.Color
import java.util.*
import kotlin.math.pow

enum class MusicParameter(val scale: () -> Float) {
    Red({ app.soundColor.red / 255f }),
    Green({ app.soundColor.green / 255f }),
    Blue({ app.soundColor.blue / 255f }),
    Bpm({ (app.avgBpm - 30) / 120 }),
    SlowAmp({ app.slowFadeAmp }),
    Amp( { app.fadeAmp } ),
    Low( { 0.1f } ),
    High( { 0.9f }),
    Minimum( { 0f } ),
    Maximum( { 0f } ),
    InverseAmp({ 1 - app.fadeAmp }),
    InverseBpm({ (1 - (app.avgBpm - 30) / 120) }),
    InverseSlowAmp({ 1 - app.slowFadeAmp }),
}

enum class FloatValue(val id: String, val initial: Float, val min: Float, val max: Float, val scale: Int) {
    BallRed("Red", 1f, 0f, 1f, 1),
    BallGreen("Green", 0f, 0f, 1f, 1),
    BallBlue("Blue", 0f, 0f, 1f, 1),
    BallAlpha("Alpha", 180f, 0f, 255f, 1),
    BallCount("Ball Count", 80f, 1f, 1000f, 3),
    BallRadius("Ball Radius", 10f, 5f, 40f, 1),
    BallStartingVel("Starting Velocity", 3f, 0f, 50f, 2),
    BallSpring("Springiness", 0.05f, 0f, 1f, 2),
    BallStick("Stickiness", 0.05f, 0f, 1f, 2),
    Gravity("Gravity", 0.1f, 0f, 1f, 2),
    Dampening("Dampening", 0.1f, 0f, 1f, 1),
    ImgScale("Image Scale", 0.2f, 0f, 1f, 1),
    LavaScale("Lava Scale", 50f, 1f, 500f, 1),
    MouseForce("Mouse Force", 1f, 0f, 10f, 2),
}

enum class BooleanValues(val id: String, val initial: Boolean) {
    ShowBalls("Show Balls", true),
    ShowLava("Show Lava", false),
    ClampLava("Clamp Lava", false),
    ShowGrid("Show Grid", false),
    ShowBackground("Show Background", false),
    InvertMouseForce("Invert Mouse Force", false),
    MusicMode("Music Mode", true),
}

open class Mode(val next: Mode?, val id: String)

enum class EnumValues(val id: String, val initial: Mode) {
    DisplayMode("Display Mode", DisplayModeValues.Normal),
    MouseForceType("Mouse Force Type", ForceTypeValues.Inverse);

    sealed class DisplayModeValues(next: Mode?, modeId: String): Mode(next, modeId) {
        data object Normal: DisplayModeValues(Grid, "Normal")
        data object Grid: DisplayModeValues(StickForce, "Grid")
        data object StickForce: DisplayModeValues(Velocity, "Stick Force")
        data object Velocity: DisplayModeValues(null, "Velocity")
    }

    sealed class ForceTypeValues(next: Mode?, modeId: String): Mode(next, modeId) {
        data object Inverse: ForceTypeValues(Linear, "Inverse")
        data object Linear: ForceTypeValues(InverseSquared, "Linear")
        data object InverseSquared: ForceTypeValues(null, "Inverse Squared")
    }
}

class Parameters {

    companion object {
        var associations = mapOf(
            Pair("Red", MusicParameter.Red),
            Pair("Green", MusicParameter.Green),
            Pair("Blue", MusicParameter.Blue),
            Pair("Alpha", MusicParameter.High),
            Pair("Ball Count", MusicParameter.SlowAmp),
            Pair("Ball Radius", MusicParameter.Low),
            Pair("Starting Velocity", MusicParameter.Bpm),
            Pair("Springiness", MusicParameter.Red),
            Pair("Stickiness", MusicParameter.Blue),
            Pair("Gravity", MusicParameter.Bpm),
            Pair("Dampening", MusicParameter.Low),
        )
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

            if (parameters[BooleanValues.MusicMode]) musicScale(associations[value.id])

            if (held) {
                val areaWidth = app.width - parameters.bounds.second.x
                val s = PApplet.map(
                    app.mouseX.toFloat(),
                    parameters.bounds.second.x + 0.1f * areaWidth,
                    app.width - 0.1f * areaWidth,
                    0f, 1f
                ).coerceIn(0f, 1f)
                parameter.set(
                    s.pow(value.scale) * (value.max - value.min) + value.min
                )
            }
        }

        fun musicScale(music: MusicParameter?) {
            parameter.set(if (music != null) {
                ((value.max - value.min) * music.scale() + value.min).coerceIn(value.min, value.max)
            } else parameter.get())
        }
    }

    class Picker(
        val height: Float,
        private val parameter: Parameter<Mode>,
        private val value: EnumValues,
    ) {
        fun display() {
            val areaWidth = app.width - parameters.bounds.second.x
            val start = parameters.bounds.second.x + 0.1f * areaWidth

            app.textAlign(PConstants.LEFT)
            app.fill(0f)
            app.text("${value.id}: ", start, height)
            app.fill(parameters.ballColor.rgb)
            val p = start + app.textWidth("${value.id}: ")
            app.text(parameter.get().id, p, height)
        }

        fun update() {
            if (!(mousePressedPulse &&
                        app.mouseX > parameters.bounds.second.x &&
                        app.mouseY > height - 10f &&
                        app.mouseY < height + 10f))
                return

            if (parameter.get().next != null) {
                parameter.set(parameter.get().next!!)
            } else {
                parameter.set(value.initial)
            }
        }
    }

    class Checkbox(
        val height: Float,
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

    class Button(
        val height: Float,
        private val action: () -> Unit,
        private val name: String,
    ) {
        fun display() {
            val areaWidth = app.width - parameters.bounds.second.x
            val start = parameters.bounds.second.x + 0.1f * areaWidth

            app.textAlign(PConstants.LEFT)
            app.fill(0f)
            app.fill(parameters.ballColor.rgb)
            app.text(name, start, height)
        }

        fun update() {
            if (!(mousePressedPulse &&
                        app.mouseX > parameters.bounds.second.x &&
                        app.mouseY > height - 10f &&
                        app.mouseY < height + 10f))
                return

            action.invoke()
        }
    }

    var floatValues = EnumMap(FloatValue.entries.associateWith { Parameter(it.initial) })
    var booleanValues = EnumMap(BooleanValues.entries.associateWith { Parameter(it.initial) })
    var enumValues = EnumMap(EnumValues.entries.associateWith { Parameter(it.initial) })

    var ballColor = getColor()
    var bounds = Pair(PVector(0f, 0f), PVector(900f, 900f))

    private val sliders = floatValues.map {
        e -> Slider((e.key.ordinal + 1) * 40f, e.value, e.key)
    }
    private val pickers = enumValues.map {
        e -> Picker((sliders.last().height + (e.key.ordinal + 2) * 20f), e.value, e.key)
    }
    private val checkboxes = booleanValues.map {
        e -> Checkbox(pickers.last().height + (e.key.ordinal + 1) * 20f, e.value, e.key.id)
    }
    private val buttons = arrayOf(
        Button(checkboxes.last().height + 20f, { saving = true }, "Save"),
        Button(checkboxes.last().height + 40f, {
//            associations = associations.keys.zip(associations.values.shuffled()).toMap()
            associations = associations.mapValues {
                MusicParameter.entries[app.random(MusicParameter.entries.size.toFloat()).toInt()]
            }
        }, "Shuffle"),
    )

    var sliderHeld = false

    fun display() {
        app.noStroke()
        app.fill(100f)
        val areaWidth = app.width - parameters.bounds.second.x
        app.rect(parameters.bounds.second.x, 0f, parameters.bounds.second.x + areaWidth, app.height.toFloat())

        sliders.forEach { slider -> slider.display() }
        checkboxes.forEach { checkbox -> checkbox.display() }
        pickers.forEach { picker -> picker.display() }
        buttons.forEach { button -> button.display() }
    }

    fun update() {
        sliders.forEach { slider -> slider.update() }
        checkboxes.forEach { checkbox -> checkbox.update() }
        pickers.forEach { picker -> picker.update() }
        buttons.forEach { button -> button.update() }

        ballColor = getColor()
    }

    private fun getColor(): Color {
        return Color(
            floatValues.getValue(FloatValue.BallRed).get(),
            floatValues.getValue(FloatValue.BallGreen).get(),
            floatValues.getValue(FloatValue.BallBlue).get(),
        )
    }

    operator fun get(key: FloatValue): Float =
        floatValues.getValue(key).get()

    operator fun get(key: BooleanValues): Boolean =
        booleanValues.getValue(key).get()

    operator fun get(key: EnumValues): Mode =
        enumValues.getValue(key).get()
}