Rewrite & New Features

Held Item Customisation
- Added fully configurable arm base position (X, Y, Z anchor and height bob scale)
- Added item transform overrides — independently control size, position (X/Y/Z), and rotation (X/Y/Z) of the held item
- Both arm position and item transform support separate values for main hand and off-hand
- Master toggle and per-feature sub-toggles for arm position and item transform independently

Swing Animation
- Added customisable swing arc — control pre-rotation, Y/Z/X arc amounts, and counter-rotation
- Added customisable swing drift — control X/Y/Z translation during the swing
- Added swing speed multiplier with optional ignore for Haste/Mining Fatigue effects
- Added option to disable swing bobbing
- Added option to disable the swing animation entirely

Entity Scaling
- Rewrote player and entity scaling to use render state instead of PoseStack transforms,
  fixing visual glitches and improving performance
- Added per-tick scale caching to avoid redundant lookups each frame
 
Other
- Added Show Own Nametag in Third Person
- Optimised mod performance