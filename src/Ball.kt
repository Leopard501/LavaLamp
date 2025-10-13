import processing.core.PApplet
import processing.core.PConstants
import processing.core.PVector
import java.awt.Color
import kotlin.math.pow

class Ball {

    var position = PVector(
        app.random(parameters.bounds.first.x, parameters.bounds.second.x),
        app.random(parameters.bounds.first.y, parameters.bounds.second.y))

    private var velocity = PVector.random2D().setMag(app.random(
        parameters[FloatValue.BallStartingVel]))
    private var cell: Grid.Cell? = null

    private var stickForce = PVector(0f, 0f)

    private val colorAge = app.random(1f)

    fun update() {
        // basic movement
        velocity.add(if (parameters[FloatValue.PolarGravity] > 0.5f) {
            PVector.fromAngle(
                (
                            position - PVector(parameters.bounds.second.x / 2, parameters.bounds.second.y / 2)
                        ).heading() +
                        parameters[FloatValue.GravityDirection]
            ).setMag(parameters[FloatValue.Gravity])
        } else {
            PVector.fromAngle(
                parameters[FloatValue.GravityDirection] + PConstants.HALF_PI
            ).setMag(parameters[FloatValue.Gravity])
        })
        position.add(velocity)

        // edge collision
        val bottom = parameters.bounds.second.y - parameters[FloatValue.BallRadius]
        val top = parameters[FloatValue.BallRadius]
        val right = parameters.bounds.second.x - parameters[FloatValue.BallRadius]
        val left = parameters[FloatValue.BallRadius]

        if (position.y > bottom) {
            position.y = bottom
            velocity.y *= -(1 - parameters[FloatValue.Dampening])
        } else if (position.y < top) {
            position.y = top
            velocity.y *= -(1 - parameters[FloatValue.Dampening])
        }
        if (position.x > right) {
            position.x = right
            velocity.x *= -(1 - parameters[FloatValue.Dampening])
        } else if (position.x < left) {
            position.x = left
            velocity.x *= -(1 - parameters[FloatValue.Dampening])
        }

        // ball interactions
        for (other in grid.getNeighborBalls(position)) {
            if (other == this) continue

            val disp = other.position - position

            // collision
            val minDist = parameters[FloatValue.BallRadius] * 2
            if (disp.mag() < minDist) {
                val target = position + disp.setMag(minDist)
                var f = (target - other.position) * parameters[FloatValue.BallSpring]
                val df = velocity * parameters[FloatValue.BallSpring] * parameters[FloatValue.Dampening]
                f += df
                velocity -= f
                other.velocity += f
            }

            // stickiness
            if (parameters[FloatValue.BallStick] > 0) {
                val dist = ((position - other.position).mag() / parameters[FloatValue.BallRadius]).coerceAtLeast(1f)
                val f = (position - other.position).setMag(1 / dist.pow(2)) * parameters[FloatValue.BallStick]
                stickForce = f
                velocity -= f
                other.velocity += f
            }
        }

        // mouse interaction
        if (parameters[FloatValue.MouseForce] > 0 && app.mousePressed && app.mouseX < parameters.bounds.second.x) {
            val mousePos = PVector(app.mouseX.toFloat(), app.mouseY.toFloat())
            val dist = ((position - mousePos).mag() / parameters[FloatValue.BallRadius]).coerceAtLeast(1f)
            val mag = when (parameters[EnumValues.MouseForceType]) {
                EnumValues.ForceTypeValues.Inverse -> 1f / dist
                EnumValues.ForceTypeValues.Linear -> dist / 100f
                EnumValues.ForceTypeValues.InverseSquared -> 1f / dist.pow(2)
                else -> {-dist}
            }
            var f = (position - mousePos).setMag(mag) * parameters[FloatValue.MouseForce]
            if (parameters[BooleanValues.InvertMouseForce]) f *= -1f
            velocity -= f
        }

        assignCell()
    }

    fun assignCell() {
        val newCell = grid.getCell(position)
        if (newCell != cell) {
            cell?.balls?.remove(this)
            newCell.balls.add(this)
            cell = newCell
        }
    }

    fun display() {
        app.noStroke()
        val i = (parameters.ballColors.size - parameters[FloatValue.ColorDelay] * colorAge)
            .toInt().coerceIn(0, parameters.ballColors.size-1)
        val c = when (parameters[EnumValues.DisplayMode]) {
            EnumValues.DisplayModeValues.Normal -> parameters.ballColors[i].rgb
            EnumValues.DisplayModeValues.Grid -> grid.colorize(position).rgb
            EnumValues.DisplayModeValues.Velocity -> {
                (Color.RED * (-velocity.y / 10f) +
                        Color.BLUE * (velocity.y / 10f) +
                        Color.MAGENTA * (-velocity.x / 10f) +
                        Color.GREEN * (velocity.x / 10f)).rgb
            }
            EnumValues.DisplayModeValues.StickForce -> {
                PApplet.lerpColor(
                    Color.BLUE.rgb, Color.RED.rgb,
                    stickForce.mag() / parameters[FloatValue.BallStick], PConstants.RGB
                )
            } else -> {
                parameters.ballColors[i].rgb
            }
        }
        app.fill(c, parameters[FloatValue.BallAlpha])
        app.circle(position.x, position.y, parameters[FloatValue.BallRadius] * 2)
    }
}