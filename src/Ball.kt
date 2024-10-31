import processing.core.PVector
import java.awt.Color
import kotlin.math.abs
import kotlin.math.min

class Ball {

    var position = PVector(
        app.random(parameters.bounds.first.x, parameters.bounds.second.x),
        app.random(parameters.bounds.first.y, parameters.bounds.second.y))

    private var velocity = PVector.random2D().setMag(app.random(
        parameters[Parameters.FloatValues.BallStartingVel]))
    private var cell: Grid.Cell? = null

    fun update() {
        // basic movement
        velocity.add(PVector(0f, parameters[Parameters.FloatValues.Gravity]))
        position.add(velocity)

        // edge collision
        val bottom = parameters.bounds.second.y - parameters[Parameters.FloatValues.BallRadius]
        val top = parameters[Parameters.FloatValues.BallRadius]
        val right = parameters.bounds.second.x - parameters[Parameters.FloatValues.BallRadius]
        val left = parameters[Parameters.FloatValues.BallRadius]

        if (position.y > bottom) {
            position.y = bottom
            velocity.y *= -parameters[Parameters.FloatValues.Dampening]
        } else if (position.y < top) {
            position.y = top
            velocity.y *= -parameters[Parameters.FloatValues.Dampening]
        }
        if (position.x > right) {
            position.x = right
            velocity.x *= -parameters[Parameters.FloatValues.Dampening]
        } else if (position.x < left) {
            position.x = left
            velocity.x *= -parameters[Parameters.FloatValues.Dampening]
        }

        // ball collision
        for (other in grid.getNeighborBalls(position)) {
            if (other == this) continue

            if ((other.position - position).mag() < parameters[Parameters.FloatValues.BallRadius] * 2) {
                val center = PVector(
                    min(position.x, other.position.x) + abs(position.x - other.position.x) / 2f,
                    min(position.y, other.position.y) + abs(position.y - other.position.y) / 2f
                )
                val damp = if (parameters[Parameters.BooleanValues.DampeningOnCollisions]) {
                    parameters[Parameters.FloatValues.Dampening]
                } else { 1f }
                velocity =
                    (velocity + (position - center)
                        .setMag(parameters[Parameters.FloatValues.BallSpring])) * damp
                other.velocity =
                    (other.velocity + (other.position - center)
                        .setMag(parameters[Parameters.FloatValues.BallSpring])) * damp
            }
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
        if (parameters[Parameters.BooleanValues.ShowGrid]) {
            app.fill(grid.colorize(position).rgb)
        } else {
            app.fill(parameters.ballColor.rgb, parameters[Parameters.FloatValues.BallAlpha])
        }
        app.circle(position.x, position.y, parameters[Parameters.FloatValues.BallRadius] * 2)
    }
}