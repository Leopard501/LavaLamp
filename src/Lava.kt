import processing.core.PApplet.*
import processing.core.PImage
import processing.core.PVector
import java.awt.Color

class Lava {

    private var img: PImage = app.createImage(
        ((parameters.bounds.second.x - parameters.bounds.first.x) * parameters[FloatValues.ImgScale]).toInt(),
        ((parameters.bounds.second.y - parameters.bounds.first.y) * parameters[FloatValues.ImgScale]).toInt(),
        RGB)

    init {
        for (x in 0..<img.width) {
            for (y in 0..<img.height) {
                var dist = MAX_FLOAT
                for (ball in grid.getNeighborBalls(
                    PVector(x.toFloat(), y.toFloat()) / parameters[FloatValues.ImgScale])
                ) {
                    dist = min(
                        (ball.position * parameters[FloatValues.ImgScale] - PVector(x.toFloat(), y.toFloat())).mag(),
                        dist,
                    )
                }
                var showDist = (dist / parameters[FloatValues.ImgScale]) / parameters[FloatValues.LavaScale]
                if (parameters[BooleanValues.ClampLava]) {
                    showDist = if (showDist > 1f) { 1f } else { 0f }
                }
                img.pixels[x + y * img.width] = lerpColor(
                    if (parameters[BooleanValues.ShowGrid]) {
                        grid.colorize(PVector(x.toFloat(), y.toFloat())
                                / parameters[FloatValues.ImgScale])
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