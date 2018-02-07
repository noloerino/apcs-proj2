                     
             /\
            / /
           / /
          / /
         / /
     ===/_/===
      ======
       /_/
                     
                     

## ABOUT
I never did get around to naming this game. Anyway, what you’re about to play is more or less a ripoff of Nintendo’s Fire Emblem series, a turn-based strategy game that first appeared back in the 90s on the NES. The player controls a lord and his small army, clearing stages of enemies to beat the story. Friendly characters that are killed in a level stay dead for the rest of the game.

This game is a rather watered-down version of Fire Emblem: there is no dialogue, no plot, and not permadeath (although characters that are killed in a level have their stats reset on the next level, mostly by accident).

By the way, this is Jonathan Shi’s second semester APCS project, 4th period 2017. Enjoy.

### NOTE:
Because of some possible quirks in the file system, it might only be possible to run this program after adding this to Eclipse.

## HOW TO PLAY:
Either select a save from the main menu, or create a new one. If no name is specified for a new save, it will be named “save” + a random 8 digit number.
The goal of each level is to clear all enemies. You lose if a critical ally character (which for now is the only one with a good sprite) is killed. If you lose, you will be returned to the main menu with your save unchanged; if you win, you will be returned to the main menu with an updated save. Selecting that save next time will then advance you to the next level.

## CONTROLS:
Press \[j\] (or whatever CONFIRM is bound to) to select a square (or go into selecting mode), and press \[k\] (or the CANCEL binding) to cancel selection.
Use WASD (or arrow keys, depending on keybindings) to move the selector.
To move a character, select them on their starting square, and then move the cursor to a tile within the blue range. Press confirm again to confirm your move. Press cancel (either through the menu bar or on the keyboard) to cancel your move.
To attack an enemy, first follow the instructions to move to a square within the attack range, then press “attack” if the option comes up. Once that’s done, attackable squares will be highlighted in orange, and you then select an enemy within that range to confirm the attack.
Press ‘l’ (or the SELECT binding) to show the attackable range of all enemies. Selecting individual enemies will allow you to see their individual ranges.

DO NOT HOLD DOWN KEYS FOR TOO LONG - according to stackoverflow, holding down some keys (usually vowels) will cause Apple keyboards to bring up a menu for diacritical markings (like é), which then breaks KeyListener.

## MAKING ASSETS:
The game uses the following folders for flexible assets:
* res/characters - specifies character assets and stats, documented
* res/classes - specifies character classes and their levelup rates and movement points
* res/items - to be implemented later
* res/stagemaps - specifies levels, documented
* res/terrain - specifies images for terrain
If a new level is added, update /res/saves/ORDER.txt for the new level order.
Any image assets not found will likely default to missing.gif
