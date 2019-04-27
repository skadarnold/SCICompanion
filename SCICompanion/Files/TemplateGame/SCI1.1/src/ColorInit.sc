;;; Sierra Script 1.0 - (do not remove this comment)
(script# COLORINIT_SCRIPT)
(include sci.sh)
(include game.sh)
(use Main)

(public
	ColorInit 0
)

;	
;	 Assigns color indices to the gColorWindowForeground, gLowlightColor and gColorWindowBackground global variables to be used in the GUI. Called at startup.
;	 You may customize this to provide custom colors for your game.
(procedure (ColorInit)
	(= gColorWindowForeground 0)
	(= gLowlightColor (Palette palFIND_COLOR 159 159 159))
	(= gColorWindowBackground 5)
)
