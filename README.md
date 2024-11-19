 Made in Kotlin as an experiment; simulates the motion and interaction of particles with realistic collision, repulsion and attraction. 
 All simulation parameters are exposed so they can be modified in real-time. 

 ## Simulation Parameters
 - **Red/Green/Blue**: Controls particle color when in normal display mode.
 - **Alpha**: Controls particle transparency. Performance may suffer if alpha is below 255 when there is a large number of particles.
 - **Ball Count**: Number of particles to simulate. Displayed number is rounded down. Performance may suffer with large numbers of particles.
 - **Ball Radius**: Controls particle display and interaction radius.
 - **Starting Velocity**: Controls particle initial velocity.
 - **Springiness**: Controls how strongly particles repel each other.
 - **Stickiness**: Controls how strongly particles attract each other.
 - **Gravity**: Strength of gravity.
 - **Dampening**: Controls what proportion of speed is lost from collisions.
 - **Image Scale**: EXPERIMENTAL; controls smearing resolution compared to window size. Performance may suffer with high image size.
 - **Lava Scale**: EXPERIMENTAL; controls smearing radius.
 - **Mouse Force**: Controls magnitude of force applied to particles on click.
 - **Display Mode**: Which mode to display particle color.
   - *Normal*: Color particles based on red/green/blue parameters.
   - *Grid*: Color particles based on what internal simulation grid cell they are contained in.
   - *Stick Force*: Color particles based on attraction force magnitude.
   - *Velocity*: Color particles based on velocity magnitude and direction.
 - **Mouse Force Type**: Selects equation for how mouse force falls off over distance from mouse.
   - *Inverse*: Force magnitude is multiplied by 1 over distance.
   - *Linear*: Force magnitude is multiplied by distance.
   - *Inverse Squared*: Force magnitude is multiplied by 1 over distance squared.
 - **Show Balls**: Display particles.
 - **Show Lava**: EXPERIMENTAL; show a 'smearing' effect based on particle position. May impact performance and simulation accuracy.
 - **Clamp Lava**: EXPERIMENTAL; round smearing opacity to 0 or 1.
 - **Show Grid**: Show internal simulation grid.
 - **Show Background**: Controls if draw buffer is cleared between frames.
 - **Invert Mouse Force**: Controls if clicking attacts or repels particles.
