;;; Sierra Script 1.0 - (do not remove this comment)
; Displays a message in response to a call to :func:`showAbout`.
(script# ABOUT_SCRIPT)
(include sci.sh)
(include game.sh)
(include 13.shm)
(use Main)
(use System)

(public
	aboutCode 0
)

(instance aboutCode of Code
	(properties)
	
	(method (doit)
		(gMessager say: N_MAIN 0 0 0 0 13)
	)
)
