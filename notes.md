# Things to save/load

- LivingEntity.swingTime
- LivingEntity.swinging
- All non-final floats and longs in DeltaTracker.Timer
- Potentially save/restore mouse movement, slightly interpolating it to prevent sudden jumps. like, if the player is 
moving the mouse left quickly and a savestate is made, when the savestate is loaded, instead of the camera immediately
stopping or changing direction to how the player is moving their mouse now, the camera will smoothly transition to the 
new direction over a short period of time
- entity nbt data (should automatically cover stuff like effects, fire, health, etc)