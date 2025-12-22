import processing.core.PApplet
import processing.core.PApplet.atan2
import processing.core.PConstants
import processing.core.PVector
import java.awt.Color
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

enum class MusicParameterGroups(val parameters: Array<Pair<String, () -> Float>>) {
    Color(arrayOf(
        Pair("Red") { app.soundColor.red / 255f },
        Pair("Green") { app.soundColor.green / 255f },
        Pair("Blue") { app.soundColor.blue / 255f },
        Pair("Cyan") { (app.soundColor.green + app.soundColor.blue) / 511f },
        Pair("Magenta") { (app.soundColor.red + app.soundColor.blue) / 511f },
        Pair("Yellow") { (app.soundColor.red + app.soundColor.green) / 511f },
    )),
    BPM(arrayOf(
        Pair("Normal") { (app.avgBpm - 30) / 120 },
        Pair("Inverse") { (1 - (app.avgBpm - 30) / 120) },
    )),
    Amp(arrayOf(
        Pair("Normal") { app.fadeAmp },
        Pair("Slow") { app.slowFadeAmp },
        Pair("Smooth") { app.smoothAmp },
        Pair("Spiky") { app.amp },
        Pair("Inverse") { 1 - app.fadeAmp },
        Pair("Inverse Slow") { 1 - app.slowFadeAmp },
        Pair("Inverse Smooth") { 1 - app.smoothAmp },
        Pair("Inverse Spiky") { 1 - app.amp },
    )),
    Constant(arrayOf(
        Pair("Minimum") { 0f },
        Pair("Low") { 0.1f },
        Pair("Medium") { 0.5f },
        Pair("High") { 0.9f },
        Pair("Maximum") { 1f },
    )),
    Beat(arrayOf(
        Pair("Fast") { app.fastBeat },
        Pair("Slow") { app.slowBeat },
        Pair("Inverse Fast") { 1 - app.fastBeat },
        Pair("Inverse Slow") { 1 - app.slowBeat },
    )),
    Rotating(arrayOf(
        Pair("Clockwise") { app.rotation },
        Pair("Counter Clockwise") { 1 - app.rotation },
        Pair("Ping-Pong") { app.pingPong },
        Pair("Pong-Ping") { 1 - app.pingPong },
    )),
    Stutter(arrayOf(
        Pair("Increasing") { app.stutter },
        Pair("Decreasing") { 1 - app.stutter },
        Pair("Random") { app.randomStutter },
    )),
    Mouse(arrayOf(
        Pair("X") { app.mouseX / parameters.bounds.second.x },
        Pair("Y") { 1 - app.mouseY / parameters.bounds.second.y },
        Pair("-X") { 1 - app.mouseX / parameters.bounds.second.x },
        Pair("-Y") { app.mouseY / parameters.bounds.second.y },
        Pair("Direction") {
            (atan2(
                app.mouseY.toFloat() - (parameters[FloatValue.PoleY] * parameters.bounds.second.y),
                app.mouseX.toFloat() - (parameters[FloatValue.PoleX] * parameters.bounds.second.x)
            ) + PConstants.PI) / PConstants.TWO_PI
        }
    ));

    companion object {
        private var weights = MusicParameterGroups.entries.map { Pair(it, app.random(50f).toInt()) }

        fun randomizeWeights() {
            weights = MusicParameterGroups.entries.map { Pair(it, app.random(1f, 50f).toInt()) }
        }

        fun selectRandom(): FloatValue.MusicParameter {
            val group = entries[app.random(entries.size.toFloat()).toInt()]
            val elem = group.parameters[app.random(group.parameters.size.toFloat()).toInt()]
            return FloatValue.MusicParameter(elem.first, group.name, elem.second)
        }

        // https://perlmaven.com/select-random-elements-from-a-weigthed-list
        fun selectWeighted(): FloatValue.MusicParameter {
            val r = app.random(weights.sumOf { it.second }.toFloat()).toInt()
            var i = 0
            var w = weights[0].second
            while (w <= r) {
                i++
                w += weights[i].second
            }
            val group = weights[i].first
            val elem = group.parameters[app.random(group.parameters.size.toFloat()).toInt()]
            return FloatValue.MusicParameter(elem.first, group.name, elem.second)
        }
    }
}

enum class FloatValue(val id: String, val initial: Float, val min: Float, val max: Float, val scale: Int,
                      var musicParameter: MusicParameter?) {
    BallRed("Red", 1f, 0f, 1f, 1, MusicParameterGroups.selectRandom()),
    BallGreen("Green", 0f, 0f, 1f, 1, MusicParameterGroups.selectRandom()),
    BallBlue("Blue", 0f, 0f, 1f, 1, MusicParameterGroups.selectRandom()),
    BallAlpha("Alpha", 180f, 25f, 255f, 2, MusicParameterGroups.selectRandom()),
    ColorDelay("Color Delay", 0f, 0f, 120f, 1, MusicParameterGroups.selectRandom()),
    BallCount("Ball Count", 80f, 25f, 1000f, 2, MusicParameterGroups.selectRandom()),
    BallRadius("Ball Radius", 0.2f, 0f, 1f, 1, MusicParameterGroups.selectRandom()),
    BallStartingVel("Starting Velocity", 3f, 0f, 50f, 2, MusicParameterGroups.selectRandom()),
    BallSpring("Springiness", 0.05f, 0f, 1f, 3, MusicParameterGroups.selectRandom()),
    BallStick("Stickiness", 0.05f, 0f, 1f, 2, MusicParameterGroups.selectRandom()),
    Gravity("Gravity", 0.1f, 0f, 1f, 2, MusicParameterGroups.selectRandom()),
    GravityDirection("Gravity Direction", 0f, 0f, PConstants.TWO_PI, 1, MusicParameterGroups.selectRandom()),
    PoleX("Pole X", 0.5f, 0f, 1f, 1, MusicParameterGroups.selectRandom()),
    PoleY("Pole Y", 0.5f, 0f, 1f, 1, MusicParameterGroups.selectRandom()),
    Dampening("Dampening", 0.1f, 0f, 1f, 1, MusicParameterGroups.selectRandom()),
    BackgroundAlpha("Background Alpha", 0f, 0f, 255f, 2, MusicParameterGroups.selectRandom()),
    MouseForce("Mouse Force", 1f, 0f, 10f, 2, null),
    ShuffleSpeed("Shuffle Speed", 30f, 0f, 120f, 2, null);

    data class MusicParameter(val name: String, val groupName: String, val function: () -> Float)
}

enum class BooleanValues(val id: String, val initial: Boolean, var musicParameter: Boolean?) {
    HorizontalBarrier("Horizontal Barrier", true, app.random(2f) > 1),
    VerticalBarrier("Vertical Barrier", true, app.random(2f) > 1),
    PolarGravity("Polar Gravity", false, app.random(2f) < 1),
    ShowGrid("Show Grid", false, null),
    InvertMouseForce("Invert Mouse Force", false, null),
    MusicMode("Music Mode", true, null),
    ParameterShuffle("Per-Parameter Shuffle", true, null),
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
        private var timer = 0

        fun display() {
            val areaWidth = if (parameters[BooleanValues.MusicMode] && value.musicParameter != null) {
                app.width - parameters.bounds.second.x - 100
            } else {
                app.width - parameters.bounds.second.x
            }
            val start = parameters.bounds.second.x + 0.1f * areaWidth
            val end = if (parameters[BooleanValues.MusicMode] && value.musicParameter != null) {
                app.width - 100 - 0.1f * areaWidth
            } else {
                app.width - 0.1f * areaWidth
            }
            val s = ((parameter.get() - value.min) / (value.max - value.min)).pow(1 / value.scale.toFloat())

            app.stroke(255f)
            app.strokeWeight(2f)
            app.line(start, height, end, height)

            app.fill(parameters.ballColors.last().rgb)
            app.noStroke()
            app.circle(start + areaWidth * 0.8f * s,
                height, 10f)

            app.textAlign(PConstants.CENTER)
            app.fill(255f)
            app.text(value.id, start + areaWidth * 0.4f, height - 10f)
            app.text(parameter.get(),
                start + areaWidth * 0.8f * s,
                height + 15f)

            // music parameter picker
            if (!parameters[BooleanValues.MusicMode] || value.musicParameter == null) return
            app.textAlign(PConstants.LEFT)
            app.fill(parameters.ballColors.last().rgb)
            app.text("${value.musicParameter!!.groupName}:", end + 0.1f * areaWidth, height - 8f)
            app.text(value.musicParameter!!.name, end + 0.1f * areaWidth, height + 8f)
        }

        fun update() {
            if (parameters[BooleanValues.MusicMode] && value.musicParameter?.function != null) {
                musicScale(value.musicParameter?.function)
                timer--
                if (timer <= 0) shuffle()
            }

            if (parameters.hidden) return

            if (!held && app.mousePressed &&
                app.mouseX > parameters.bounds.second.x &&
                if (parameters[BooleanValues.MusicMode] && value.musicParameter != null) {
                    app.mouseX < app.width - 100
                } else { true } &&
                app.mouseY > height - 10f &&
                app.mouseY < height + 10f &&
                !parameters.sliderHeld) {
                held = true
                parameters.sliderHeld = true
            }; if (held && !app.mousePressed) {
                held = false
                parameters.sliderHeld = false
            }
            val areaWidth = if (parameters[BooleanValues.MusicMode] && value.musicParameter != null) {
                app.width - parameters.bounds.second.x - 100
            } else {
                app.width - parameters.bounds.second.x
            }
            val end = if (parameters[BooleanValues.MusicMode] && value.musicParameter != null) {
                app.width - 100 - 0.1f * areaWidth
            } else {
                app.width - 0.1f * areaWidth
            }

            if (held) {
                val s = PApplet.map(
                    app.mouseX.toFloat(),
                    parameters.bounds.second.x + 0.1f * areaWidth,
                    end,
                    0f, 1f
                ).coerceIn(0f, 1f)
                parameter.set(
                    s.pow(value.scale) * (value.max - value.min) + value.min
                )
            }

            // music parameter picker
            if (!parameters[BooleanValues.MusicMode] || value.musicParameter == null) return
            if (!(mousePressedPulse &&
                        app.mouseX > end + 0.1f * areaWidth &&
                        app.mouseY > height - 10f &&
                        app.mouseY < height + 10f))
                return
        }

        fun shuffle() {
            if (value.musicParameter == null) return

            if (app.random(10f) < 1)
                MusicParameterGroups.randomizeWeights()
            value.musicParameter = MusicParameterGroups.selectWeighted()
            timer = (parameters[FloatValue.ShuffleSpeed] *
                    if (parameters[BooleanValues.ParameterShuffle]) {
                        parameters[FloatValue.ShuffleSpeed] * 2f.pow(app.random(-2f, 3f).toInt())
                    } else { 1f }).toInt()
        }

        private fun musicScale(musicFunction: (() -> Float)?) {
            parameter.set(if (musicFunction != null) {
                ((value.max - value.min) * musicFunction().pow(value.scale) + value.min).coerceIn(value.min, value.max)
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
            app.fill(255f)
            app.text("${value.id}: ", start, height)
            app.fill(parameters.ballColors.last().rgb)
            val p = start + app.textWidth("${value.id}: ")
            app.text(parameter.get().id, p, height)
        }

        fun update() {
            if (parameters.hidden) return

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
        private val value: BooleanValues
    ) {
        private var timer = 0

        fun display() {
            val areaWidth = app.width - parameters.bounds.second.x
            val start = parameters.bounds.second.x + 0.1f * areaWidth
            val end = app.width - 100 - 0.1f * areaWidth

            app.stroke(255f)
            app.strokeWeight(2f)
            if (v.get()) app.fill(parameters.ballColors.last().rgb)
            else app.noFill()
            app.rect(start, height - 10f, start + 10f, height)

            app.textAlign(PConstants.LEFT)
            app.fill(255f)
            app.text(value.id, start + 10f + 10f, height)

            if (!parameters[BooleanValues.MusicMode] || value.musicParameter == null) return
            app.textAlign(PConstants.LEFT)
            app.fill(parameters.ballColors.last().rgb)
            app.text(if (value.musicParameter!!) { "Enabled" } else { "Disabled" }, end + 0.1f * areaWidth, height)
        }

        fun update() {
            if (parameters.hidden) return

            if (parameters[BooleanValues.MusicMode]) {
                timer--
                if (timer <= 0) shuffle()
            }

            if (!(mousePressedPulse &&
                app.mouseX > parameters.bounds.second.x &&
                app.mouseY > height - 10f &&
                app.mouseY < height + 10f))
                return

            v.set(!v.get())
        }

        fun shuffle() {
            if (value.musicParameter == null) return

            value.musicParameter = app.random(2f) < 1
            v.set(value.musicParameter!!)
            timer = (parameters[FloatValue.ShuffleSpeed] *
                    if (parameters[BooleanValues.ParameterShuffle]) {
                        parameters[FloatValue.ShuffleSpeed] * 2f.pow(app.random(-2f, 3f).toInt())
                    } else { 1f }).toInt()
        }
    }

    class Button(
        private val height: Float,
        private val action: () -> Unit,
        private val name: String,
    ) {
        fun display() {
            val areaWidth = app.width - parameters.bounds.second.x
            val start = parameters.bounds.second.x + 0.1f * areaWidth

            app.textAlign(PConstants.LEFT)
            app.fill(parameters.ballColors.last().rgb)
            app.text(name, start, height)
        }

        fun update() {
            if (parameters.hidden) return

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
    private var enumValues = EnumMap(EnumValues.entries.associateWith { Parameter(it.initial) })

    var ballColors = LimitedStack<Color>(FloatValue.ColorDelay.max.toInt())
    var bounds = Pair(
        PVector(0f, 0f),
        PVector(app.width - 300f, app.height.toFloat())
    )
    private var maxBallRadius = 10f
    var adjustedBallRadius = 10f
    var hidden = false

    private val sliders = floatValues.map {
        e -> Slider((e.key.ordinal + 1) * 40f, e.value, e.key)
    }
    private val pickers = enumValues.map {
        e -> Picker((sliders.last().height + (e.key.ordinal + 2) * 20f), e.value, e.key)
    }
    private val checkboxes = booleanValues.map {
        e -> Checkbox(pickers.last().height + (e.key.ordinal + 1) * 20f, e.value, e.key)
    }
    private val buttons = arrayOf(
        Button(checkboxes.last().height + 20f, { saving = true }, "Save"),
        Button(checkboxes.last().height + 40f, {
            sliders.forEach { it.shuffle() }
            checkboxes.forEach { it.shuffle() } }, "Shuffle"),
        Button(checkboxes.last().height + 60f, { parameters.toggleHidden() }, "Hide"),
    )

    var sliderHeld = false

    fun display() {
        if (hidden) return

        app.noStroke()
        app.fill(0)
        val areaWidth = app.width - parameters.bounds.second.x
        app.rect(parameters.bounds.second.x, 0f, parameters.bounds.second.x + areaWidth, app.height.toFloat())

        sliders.forEach { it.display() }
        checkboxes.forEach { it.display() }
        pickers.forEach { it.display() }
        buttons.forEach { it.display() }
    }

    fun update() {
        if (hidden) {
            if (app.mouseX > app.width - 50f && app.mousePressed) {
                toggleHidden()
            }
        }

        sliders.forEach { slider -> slider.update() }
        checkboxes.forEach { checkbox -> checkbox.update() }
        pickers.forEach { picker -> picker.update() }
        buttons.forEach { button -> button.update() }

        maxBallRadius = sqrt(
            (parameters.bounds.second.x * parameters.bounds.second.y) / parameters[FloatValue.BallCount]
        )
        adjustedBallRadius = parameters[FloatValue.BallRadius] * parameters.maxBallRadius * 0.25f + 5f

        ballColors.push(getColor())
    }

    private fun toggleHidden() {
        if (hidden) {
            bounds = Pair(
                PVector(0f, 0f),
                PVector(app.width - 300f, app.height.toFloat())
            )
            hidden = false
        } else {
            bounds = Pair(
                PVector(0f, 0f),
                PVector(app.width.toFloat(), app.height.toFloat())
            )
            hidden = true
        }
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