import processing.core.PApplet
import processing.core.PVector
import processing.sound.Amplitude
import processing.sound.AudioIn
import processing.sound.FFT
import java.awt.Color
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow

var app = Main()

var mousePressedPulse = false
var mouseReleasedPulse = false

val balls: ArrayList<Ball> = ArrayList()

lateinit var parameters: Parameters
lateinit var grid: Grid

var saving = false

fun main() {
    PApplet.main("Main")
}

class LimitedStack<T>(val max: Int): Stack<T>() {

    override fun push(item: T?): T? {
        val r = super.push(item)

        if (size > max) {
            removeFirst()
        }

        return r
    }
}

class Beat() {

    val beats = LimitedStack<Pair<Float, Int>>(1100)
    val thresholdScale = 0.993f

    var threshold = 0f
    var delays = LimitedStack<Int>(10)

    fun push(value: Float, time: Int) {
        if (value > threshold && value - threshold > 0.02f) {
            beats.push(Pair(value, time))
            threshold = value
            if (beats.size > 1) {
                delays.push(beats[beats.size-1].second - beats[beats.size-2].second)
            }
        }
    }

    fun update() {
        threshold *= thresholdScale
    }

    fun getBpm(): Float {
        if (beats.isEmpty() || delays.sum() <= 0) return 0f
        return 60_000 / (delays.sum() / delays.size.toFloat())
    }
}

class Main: PApplet() {

    val bands = 16
    val fadeRate = 0.99f
    val slowFadeRate = 0.998f
    val weights = FloatArray(bands) { i -> (i + 1f).pow(2.4f) }

    lateinit var soundInp: AudioIn
    lateinit var soundAmp: Amplitude
    lateinit var soundFft: FFT

    lateinit var beatsFft: Array<Beat>
    var avgBpm = 0f

    var amp = 0f
    var fft: FloatArray = FloatArray(bands) { 0f }
    var fadeAmp = 0f
    var smoothAmp = 0f
    var slowFadeAmp = 0f
    var fadeFft: FloatArray = FloatArray(bands) { 0f }
    var soundColor: Color = Color.BLACK
    var shuffleTime = 0
    var delayedShuffleSpeed = 0f
    var rotation = 0f
    var pingPong = 0f
    var pingPongDirection = 1

    override fun settings() {
        size(1100, 900)
        fullScreen()

        app = this
    }

    override fun setup() {
        surface.setTitle("Lava Lamp")
        rectMode(CORNERS)

        soundSetup()

        parameters = Parameters()
        repeat(parameters[FloatValue.BallCount].toInt()) {
            balls.add(Ball())
        }
        grid = Grid()
        balls.forEach { ball -> ball.assignCell() }

        // initialize to black
        fill(0f)
        rect(parameters.bounds.first.x, parameters.bounds.first.y,
            parameters.bounds.second.x, parameters.bounds.second.y)
    }

    override fun draw() {
        update()
        display()

        mousePressedPulse = false
        mouseReleasedPulse = false
    }

    private fun update() {
        soundUpdate()

        balls.forEach { ball -> ball.update() }

        val oldFloats = EnumMap(parameters.floatValues.map { Pair(it.key, it.value.get()) }.toMap())
        EnumMap(parameters.booleanValues.map { Pair(it.key, it.value.get()) }.toMap())
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

        if (oldFloats.getValue(FloatValue.BallRadius) != parameters[FloatValue.BallRadius]) {
            grid = Grid()
            balls.forEach { ball -> ball.assignCell() }
        }
    }

    private fun display() {
        fill(0f, parameters[FloatValue.BackgroundAlpha])
        noStroke()
        rect(parameters.bounds.first.x, parameters.bounds.first.y,
                parameters.bounds.second.x, parameters.bounds.second.y)

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

    fun soundSetup() {
        soundAmp = Amplitude(this)
        soundFft = FFT(this, bands)
        beatsFft = Array(bands) { Beat() }

        soundInp = AudioIn(this, 0)
        soundInp.start()

        soundAmp.input(soundInp)
        soundFft.input(soundInp)
    }

    fun soundUpdate() {
        // amp
        amp = soundAmp.analyze()
        fadeAmp = max(amp, fadeAmp * fadeRate)
        slowFadeAmp = max(fadeAmp, slowFadeAmp * slowFadeRate)
        smoothAmp = (amp + smoothAmp) / 2

        // fft
        var f = FloatArray(bands) { 0f }
        soundFft.analyze(f)
        f = f.mapIndexed { i, e -> e * weights[i] }.toFloatArray()
        if (f.last() < 0.01f || f.last() / fft.last() < 50) { // catch noise
            fft = f
        }
        fadeFft = fadeFft.mapIndexed { i, e -> max(fft[i], e * fadeRate) }.toFloatArray()

        // beats fft
        fadeFft.forEachIndexed {
            i, it -> beatsFft[i].push(it, millis())
        }
        beatsFft.forEach { it.update() }
        avgBpm = beatsFft.map { it.getBpm() }.sum() / beatsFft.size

        // color
        soundColor = Color.BLACK
        for ((i, band) in fadeFft.withIndex()) {
            val c = waveLengthToRGB(1 - ((i + 1) / bands.toFloat()))
            soundColor += c * band * (1 / bands.toFloat())
        }

        // reset shuffle time when value changed
        if (delayedShuffleSpeed != parameters[FloatValue.ShuffleSpeed]) {
            delayedShuffleSpeed = parameters[FloatValue.ShuffleSpeed]
            shuffleTime = millis()
        }

        // auto shuffle
        val shuffleTarget = parameters[FloatValue.ShuffleSpeed] * 1000
        if (shuffleTarget > 1000 && millis() - shuffleTime > shuffleTarget) {
            shuffleSound()
            shuffleTime = millis()
        }

        // rotation
        rotation += (avgBpm / (3600 * 6))
        if (rotation > 1) rotation -= 1

        // ping pong
        pingPong += (avgBpm / (3600 * 6)) * pingPongDirection
        if (pingPong >= 1) {
            pingPongDirection = -1
            pingPong = 1f
        }
        if (pingPong <= 0) {
            pingPongDirection = 1
            pingPong = 0f
        }
    }

    fun shuffleSound() {
        FloatValue.entries.forEach {
            if (it.musicParameter != null) {
                it.musicParameter = MusicParameter.entries[app.random(MusicParameter.entries.size.toFloat()).toInt()]
            }
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

/**
 * Taken from Earl F. Glynn's web page:
 * [Spectra Lab Report](http://www.efg2.com/Lab/ScienceAndEngineering/Spectra.htm)
 */
fun waveLengthToRGB(scale: Float): Color {
    val red: Float
    val green: Float
    val blue: Float
    val wavelength = scale * 400 + 380

    if ((wavelength >= 380) && (wavelength < 440)) {
        red = -(wavelength - 440) / (440 - 380)
        green = 0f
        blue = 1f
    } else if ((wavelength >= 440) && (wavelength < 490)) {
        red = 0f
        green = (wavelength - 440) / (490 - 440)
        blue = 1f
    } else if ((wavelength >= 490) && (wavelength < 510)) {
        red = 0f
        green = 1f
        blue = -(wavelength - 510) / (510 - 490)
    } else if ((wavelength >= 510) && (wavelength < 580)) {
        red = (wavelength - 510) / (580 - 510)
        green = 1f
        blue = 0f
    } else if ((wavelength >= 580) && (wavelength < 645)) {
        red = 1f
        green = -(wavelength - 645) / (645 - 580)
        blue = 0f
    } else if ((wavelength >= 645) && (wavelength < 781)) {
        red = 1f
        green = 0f
        blue = 0f
    } else {
        red = 0f
        green = 0f
        blue = 0f
    }

    return Color(red, green, blue)
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