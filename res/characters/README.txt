HOW TO READ FOLDER STUFF
Each unit is given a folder with the directory in which the unit may be found, which should be the same as the unit's in-game name. The folder should contain the following:
* A folder called "base_anim", containing the gifs for the character's default map animation
* A folder called “moving”, containing gifs for the character’s moving animations left, right, up, and down.
* A folder called “attacking”, with the same specifications as moving.
* A file called "default.gif", containing the character's default sprite (to be used in dialogue if ever implemented)
* A text file titled "start_info.txt", specifying any of the following fields:
	* NAME :: String
	* DESCRIPTION :: String
	* SIDE :: Alignment
	* CLASS :: GameCharacterClass
	* SYM_ANIM :: Bool
	* IS_MISSION_CRITICAL :: Bool
* A text file titled “start_stats.txt”, specifying any of the following fields:
	* LEVEL :: Int
	* EXP :: Int
	* HP :: Int
	* ATK :: Int
	* SPD :: Int
	* DEF :: Int
	* RES :: Int