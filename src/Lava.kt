import processing.core.PApplet.*
import processing.core.PImage
import processing.core.PVector
import java.awt.Color

class Lava {

    private lateinit var img: PImage

    init {
        createImg()
    }

    fun createImg() {
        img = app.createImage(
            ((parameters.bounds.second.x - parameters.bounds.first.x) * parameters.getFloat(Parameters.FloatValues.ImgScale)).toInt(),
            ((parameters.bounds.second.y - parameters.bounds.first.y) * parameters.getFloat(Parameters.FloatValues.ImgScale)).toInt(),
            RGB)
    }

    fun update() {
        createImg()

        for (x in 0..<img.width) {
            for (y in 0..<img.height) {
                var dist = MAX_FLOAT
                for (ball in grid.getNeighborBalls(
                    PVector(x.toFloat(), y.toFloat()) / parameters.getFloat(Parameters.FloatValues.ImgScale))
                ) {
                    dist = min(
                        (ball.position * parameters.getFloat(Parameters.FloatValues.ImgScale)
                                - PVector(x.toFloat(), y.toFloat())).mag(),
                        dist,
                    )
                }
                var showDist = dist * (1 - parameters.getFloat(Parameters.FloatValues.LavaScale))
                if (parameters.getBoolean(Parameters.BooleanValues.ClampLava)) {
                    showDist = if (showDist > 1f) { 1f } else { 0f }
                }
                img.pixels[x + y * img.width] = lerpColor(
                    if (parameters.getBoolean(Parameters.BooleanValues.ShowGrid)) {
                        grid.colorize(PVector(x.toFloat(), y.toFloat())
                                / parameters.getFloat(Parameters.FloatValues.ImgScale))
                    } else {
                        parameters.ballColor
                    }.rgb,
                    Color.WHITE.rgb, showDist, RGB)
            }
        }
    }

    fun display() {
        app.image(img, parameters.bounds.first.x, parameters.bounds.first.y,
            parameters.bounds.second.x - parameters.bounds.first.x,
            parameters.bounds.second.y - parameters.bounds.first.y)
    }
}