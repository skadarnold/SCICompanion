;;; Sierra Script 1.0 - (do not remove this comment)
(script# 110)
(include sci.sh)
(include Verbs.sh)
(include game.sh)
(include 110.shm)
(include 110.shp)
(use Main)
(use DisposeLoad)
(use Sound)
(use Cycle)
(use Game)
(use Actor)
(use System)
(use Print)
(use Polygon)

(public
	rm110 0
)

(instance rm110 of Room
	(properties
		picture 110
		style (| dpANIMATION_BLACKOUT dpOPEN_FADEPALETTE)
		horizon 50
		vanishingX 130
		vanishingY 50
		noun N_ROOM
	)
	
	(method (init)
		(gRoom addObstacle: (&getpoly ""))
		(super init:)
		(switch gPreviousRoomNumber
			; Add room numbers here to set up the ego when coming from different directions.
			(else 
				(SetUpEgo -1 1)
				(gEgo posn: 150 130)
			)
		)
		(gEgo init:)
		; We just came from the title screen, so we need to call this to give control
		; to the player.
		(gGame handsOn:)
	)
)
